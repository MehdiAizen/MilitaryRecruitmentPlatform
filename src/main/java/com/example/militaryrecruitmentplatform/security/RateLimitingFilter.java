package com.example.militaryrecruitmentplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // FIX: le context-path est /api donc l'URI réelle est /api/auth/login
        boolean isLoginEndpoint = (uri.equals("/api/auth/login") || uri.equals("/auth/login"))
                && "POST".equalsIgnoreCase(request.getMethod());

        if (!isLoginEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        // FIX: vérification AVANT le traitement — si bloqué, on rejette immédiatement
        // (ne pas attendre après filterChain.doFilter qui peut avoir commité la réponse)
        if (loginAttemptService.isBlocked(clientIp)) {
            log.warn("Tentative login bloquée - IP: {}", clientIp);
            long minutesRemaining = loginAttemptService.getBlockTimeRemaining(clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\": \"Trop de tentatives. Réessayez dans " + minutesRemaining + " minute(s).\"}"
            );
            return;
        }

        // FIX: on ne lit pas response.getStatus() après doFilter car la réponse peut être commitée.
        // Le comptage des échecs est géré directement dans AuthService.login()
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}