// service/AuthService.java
package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.dto.LoginRequest;
import com.example.militaryrecruitmentplatform.dto.LoginResponse;
import com.example.militaryrecruitmentplatform.model.Role;
import com.example.militaryrecruitmentplatform.model.User;
import com.example.militaryrecruitmentplatform.repository.UserRepository;
import com.example.militaryrecruitmentplatform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String jwt = jwtTokenProvider.generateToken(authentication);

        return new LoginResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole()
        );
    }

    public void initAdminUser() {
        if (!userRepository.existsByEmail("admin@defense-test.tn")) {
            User admin = new User();
            admin.setEmail("admin@defense-test.tn");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            User rh = new User();
            rh.setEmail("rh@defense-test.tn");
            rh.setPasswordHash(passwordEncoder.encode("rh123"));
            rh.setNom("Trabelsi");
            rh.setPrenom("Salma");
            rh.setRole(Role.RH);
            userRepository.save(rh);

            User commandant = new User();
            commandant.setEmail("cmd@defense-test.tn");
            commandant.setPasswordHash(passwordEncoder.encode("cmd123"));
            commandant.setNom("Jebali");
            commandant.setPrenom("Karim");
            commandant.setRole(Role.COMMANDANT);
            userRepository.save(commandant);
        }
    }
}