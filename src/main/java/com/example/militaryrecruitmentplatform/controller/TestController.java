package com.example.militaryrecruitmentplatform.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final JavaMailSender mailSender;

    @GetMapping("/email")
    @PermitAll
    public String testEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("souhajouha1412@gmail.com"); // ← ton email réel pour recevoir le test
        message.setSubject("Félicitations ! Votre candidature a été acceptée – Ministère de la Défense");
        message.setText("""
                Cher(e) Souha Jomaa,

                Nous avons le plaisir de vous informer que votre candidature pour intégrer l'unité 
                "Garde Nationale" a été ACCEPTÉE.

                Votre dossier a été validé par le Commandant Salma Trabelsi en date du 25 février 2026 à 14:30.

                Prochaines étapes :
                - Vous serez contacté(e) très prochainement pour les formalités administratives et médicales.
                - Signature de votre engagement.
                - Intégration dans l'unité sélectionnée.

                Nous vous remercions pour votre engagement et votre volonté de servir votre pays.

                Pour plus d'informations ou pour consulter votre dossier, connectez-vous à votre espace personnel.

                Cordialement,
                Direction du Recrutement et de la Sélection
                Ministère de la Défense Nationale
                République Tunisienne
                """);
        message.setFrom("souhajomaa1412@gmail.com"); // ← identique à spring.mail.username

        try {
            mailSender.send(message);
            log.info("Email de test ACCEPTATION envoyé avec succès à souhajouha1412@gmail.com");
            return """
                    Email de test ACCEPTATION envoyé avec succès !
                    Vérifiez votre boîte de réception, SPAM et dossier "Promotions".
                    Sujet : Félicitations ! Votre candidature a été acceptée – Ministère de la Défense
                    Temps estimé : 1 à 5 minutes (Gmail peut retarder)
                    """;
        } catch (Exception e) {
            log.error("Échec envoi email test acceptation", e);
            return "Échec envoi email : " + e.getClass().getSimpleName() + " → " + e.getMessage();
        }
    }
}