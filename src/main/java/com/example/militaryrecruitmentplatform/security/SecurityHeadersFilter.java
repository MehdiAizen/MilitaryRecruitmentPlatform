package com.example.militaryrecruitmentplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Protection contre le Clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Empêche le navigateur de deviner le type MIME (sécurité XSS)
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Active le mode strict du navigateur (XSS)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Politique de sécurité du contenu (CSP) - De base ici
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");

        // HSTS (Force HTTPS si déployé en prod)
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        filterChain.doFilter(request, response);
    }
}