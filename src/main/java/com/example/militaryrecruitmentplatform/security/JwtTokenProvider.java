package com.example.militaryrecruitmentplatform.security;

import com.example.militaryrecruitmentplatform.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.error("JWT secret trop court! Minimum 32 caractères requis.");
            throw new IllegalStateException("Configuration JWT invalide");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        String jti = UUID.randomUUID().toString();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return Jwts.builder()
                .id(jti)
                .subject(userDetails.getUsername())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        String jti = UUID.randomUUID().toString();

        String role = userRepository.findByEmail(username)
                .map(u -> "ROLE_" + u.getRole().name())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + username));

        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs * 7L);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String getJtiFromJWT(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getId() : null;
    }

    public Date getExpirationDateFromJWT(String token) {
        Claims claims = parseClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * Valide un token JWT pour les requêtes normales.
     * Retourne false si le token est expiré, blacklisté ou invalide.
     */
    public boolean validateToken(String authToken) {
        try {
            if (isTokenBlacklisted(authToken)) {
                log.warn("Token révoqué utilisé");
                return false;
            }

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);

            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("Token JWT expiré");
        } catch (UnsupportedJwtException ex) {
            log.warn("Token JWT non supporté: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Token JWT malformé: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("Signature JWT invalide: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string vide: {}", ex.getMessage());
        }
        return false;
    }

    // FIX: nouvelle méthode pour le endpoint /auth/refresh
    // validateToken() retournait false pour les tokens expirés, ce qui rendait
    // le refresh impossible lorsque le navigateur revenait d'une mise en veille.
    // Cette méthode vérifie la signature et la blacklist, mais IGNORE l'expiration.
    public boolean validateTokenAllowExpired(String authToken) {
        try {
            if (isTokenBlacklisted(authToken)) {
                log.warn("Token révoqué utilisé pour refresh");
                return false;
            }

            // parseClaimsJws lève ExpiredJwtException pour les tokens expirés
            // On la capture et on retourne true quand même (signature valide = token authentique)
            try {
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(authToken);
                return true; // token valide et non expiré
            } catch (ExpiredJwtException ex) {
                // Token expiré mais signature valide → autorisé pour le refresh
                log.debug("Token expiré accepté pour refresh - username: {}", ex.getClaims().getSubject());
                return true;
            }

        } catch (UnsupportedJwtException ex) {
            log.warn("Token JWT non supporté: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Token JWT malformé: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("Signature JWT invalide: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string vide: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * getUsernameFromJWT pour tokens expirés (utilisé dans le refresh)
     */
    public String getUsernameFromExpiredJWT(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            // Si on arrive ici le token n'est pas expiré, on utilise la méthode normale
            return getUsernameFromJWT(token);
        } catch (ExpiredJwtException ex) {
            // On récupère le username depuis l'exception elle-même
            return ex.getClaims().getSubject();
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean isTokenBlacklisted(String token) {
        // FIX: pour les tokens expirés, parseClaims retourne null → on extrait le jti autrement
        String jti = getJtiSafe(token);
        if (jti == null) return false;

        Long expiry = tokenBlacklist.get(jti);
        if (expiry == null) return false;

        if (System.currentTimeMillis() > expiry) {
            tokenBlacklist.remove(jti);
            return false;
        }
        return true;
    }

    public void revokeToken(String token) {
        String jti = getJtiSafe(token);
        Date expiration = getExpirationSafe(token);

        if (jti != null && expiration != null) {
            tokenBlacklist.put(jti, expiration.getTime());
            log.info("Token révoqué (blacklist mémoire): {}", jti);
        }
    }

    public void revokeAllUserTokens(String username) {
        log.info("revokeAllUserTokens appelé pour: {} (mode mémoire)", username);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    // FIX: extrait le jti même si le token est expiré
    private String getJtiSafe(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getId();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims().getId();
        } catch (Exception e) {
            return null;
        }
    }

    // FIX: extrait la date d'expiration même si le token est expiré
    private Date getExpirationSafe(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims().getExpiration();
        } catch (Exception e) {
            return null;
        }
    }
}