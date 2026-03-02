package com.example.militaryrecruitmentplatform.controller;

import com.example.militaryrecruitmentplatform.dto.LoginRequest;
import com.example.militaryrecruitmentplatform.dto.LoginResponse;
import com.example.militaryrecruitmentplatform.dto.RegisterDTO;
import com.example.militaryrecruitmentplatform.dto.UserRequest;
import com.example.militaryrecruitmentplatform.dto.UserResponse;
import com.example.militaryrecruitmentplatform.model.Role;
import com.example.militaryrecruitmentplatform.security.JwtTokenProvider;
import com.example.militaryrecruitmentplatform.service.AuthService;
import com.example.militaryrecruitmentplatform.service.MfaService;
import com.example.militaryrecruitmentplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MfaService mfaService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    // FIX: endpoint /auth/register ajouté — il était manquant complètement
    // Register.jsx appelait POST /auth/register mais aucun handler n'existait → 404
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            UserRequest userRequest = new UserRequest(
                    dto.getEmail(),
                    dto.getPassword(),
                    dto.getNom(),
                    dto.getPrenom(),
                    Role.CANDIDAT  // Les nouveaux inscrits sont toujours CANDIDAT
            );
            UserResponse created = userService.createUser(userRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Compte créé avec succès",
                            "email", created.getEmail()
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    // FIX: le refresh accepte maintenant les tokens expirés
    // Avant : validateToken() retournait false pour un token expiré → impossible de rafraîchir
    // Maintenant : on utilise validateTokenAllowExpired() qui vérifie la signature sans rejeter l'expiration
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Token manquant"));
        }
        // FIX: utilise la méthode qui tolère l'expiration
        if (!jwtTokenProvider.validateTokenAllowExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token invalide (signature incorrecte ou blacklisté)"));
        }
        String username = jwtTokenProvider.getUsernameFromJWT(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token invalide"));
        }
        jwtTokenProvider.revokeToken(token);
        String newToken = jwtTokenProvider.generateTokenFromUsername(username);
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    @GetMapping("/mfa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaService.MfaSetupDetails> setupMfa() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(mfaService.generateSecret(email));
    }

    @PostMapping("/mfa/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> verifyAndEnableMfa(@RequestParam int code) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean success = mfaService.enableMfa(email, code);
        if (success) {
            return ResponseEntity.ok("MFA activé avec succès !");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code invalide.");
        }
    }
}