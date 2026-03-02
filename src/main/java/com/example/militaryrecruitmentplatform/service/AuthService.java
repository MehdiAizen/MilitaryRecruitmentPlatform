package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.dto.LoginRequest;
import com.example.militaryrecruitmentplatform.dto.LoginResponse;
import com.example.militaryrecruitmentplatform.model.User;
import com.example.militaryrecruitmentplatform.repository.UserRepository;
import com.example.militaryrecruitmentplatform.security.JwtTokenProvider;
import com.example.militaryrecruitmentplatform.security.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        if (loginAttemptService.isBlocked(ip)) {
            throw new BadCredentialsException("Trop de tentatives échouées. Réessayez dans 30 secondes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String jwt = jwtTokenProvider.generateToken(authentication);

            loginAttemptService.loginSucceeded(ip);

            return new LoginResponse(
                    jwt,
                    user.getId(),
                    user.getEmail(),
                    user.getNom(),
                    user.getPrenom(),
                    user.getRole()
            );

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(ip);
            throw e;

        } catch (Exception e) {
            loginAttemptService.loginFailed(ip);
            throw e;
        }
    }

    // FIX: initAdminUser() supprimé — c'était du code mort jamais appelé.
    // L'initialisation des utilisateurs est gérée exclusivement par DataInitializer.java
    // via @PostConstruct, ce qui est le bon endroit.
}