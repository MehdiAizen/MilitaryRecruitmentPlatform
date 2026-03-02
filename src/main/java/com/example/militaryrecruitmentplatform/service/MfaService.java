package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.model.User;
import com.example.militaryrecruitmentplatform.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final UserRepository userRepository;

    public MfaSetupDetails generateSecret(String userEmail) {
        final GoogleAuthenticatorKey key = gAuth.createCredentials(userEmail);

        String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                "Military Recruitment",
                userEmail,
                key
        );

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMfaSecret(key.getKey());
        user.setMfaEnabled(false);
        userRepository.save(user);

        return new MfaSetupDetails(key.getKey(), qrUrl);
    }

    public boolean enableMfa(String userEmail, int code) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getMfaSecret() == null) {
            throw new RuntimeException("MFA non initialisé.");
        }

        boolean isCodeValid = gAuth.authorize(user.getMfaSecret(), code);

        if (isCodeValid) {
            user.setMfaEnabled(true);
            userRepository.save(user);
        }

        return isCodeValid;
    }

    public boolean verifyCode(String userEmail, int code) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isMfaEnabled()) return true;

        return gAuth.authorize(user.getMfaSecret(), code);
    }

    public record MfaSetupDetails(String secretKey, String qrCodeUrl) {}
}