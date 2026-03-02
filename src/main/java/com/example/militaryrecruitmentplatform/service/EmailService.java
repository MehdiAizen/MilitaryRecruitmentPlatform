package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.model.Candidature;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.rh:rh@defense.tn}")
    private String rhEmail;

    @Value("${spring.mail.username:no-reply@defense.tn}")
    private String fromEmail;

    /**
     * Confirmation de réception de candidature (candidat)
     */
    public void sendCandidatConfirmation(Candidature c) {
        // FIX: Gestion des candidats anonymes ET connectés
        String to;
        if (c.getCandidat() != null && c.getCandidat().getEmail() != null) {
            to = c.getCandidat().getEmail();
        } else if (c.getEmail() != null) {
            to = c.getEmail();
        } else {
            log.warn("Impossible d'envoyer confirmation : pas d'email (ID: {})", c.getId());
            return;
        }

        sendHtmlEmail(
                to,
                "Candidature reçue – Ministère de la Défense Nationale",
                buildConfirmationContent(c)
        );
    }

    /**
     * Notification à l'équipe RH d'une nouvelle candidature
     */
    public void sendRHNotification(Candidature c) {
        String unite = c.getUniteChoisie() != null ? escapeHtml(c.getUniteChoisie().getNom()) : "non spécifiée";

        sendHtmlEmail(
                rhEmail,
                "Nouvelle candidature reçue – Unité : " + unite,
                buildRHNotificationContent(c)
        );
    }

    /**
     * Mise à jour du statut (acceptée / rejetée / en examen)
     */
    public void sendStatutUpdate(Candidature c, String commentaire) {
        // FIX: Gestion des candidats anonymes ET connectés
        String to;
        if (c.getCandidat() != null && c.getCandidat().getEmail() != null) {
            to = c.getCandidat().getEmail();
        } else if (c.getEmail() != null) {
            to = c.getEmail();
        } else {
            log.warn("Impossible d'envoyer mise à jour statut : pas d'email (ID: {})", c.getId());
            return;
        }

        String subject = switch (c.getStatut()) {
            case VALIDEE -> "Félicitations ! Votre candidature a été acceptée";
            case REJETEE -> "Votre candidature n'a pas été retenue";
            case EN_EXAMEN -> "Votre candidature est en cours d'examen";
            default -> "Mise à jour de votre candidature";
        };

        sendHtmlEmail(
                to,
                subject,
                buildStatutUpdateContent(c, commentaire)
        );
    }

    // Méthode générique d'envoi HTML
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email HTML envoyé avec succès à {} - Sujet: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Échec envoi email HTML à {} - Sujet: {} - Erreur: {}", to, subject, e.getMessage(), e);
        }
    }

    private String buildConfirmationContent(Candidature c) {
        String unite = c.getUniteChoisie() != null ? escapeHtml(c.getUniteChoisie().getNom()) : "non spécifiée";

        return """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { color: #1e3a5f; text-align: center; }
                        .button { background-color: #1e3a5f; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; }
                        .recap { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }
                        .footer { font-size: 14px; color: #666; margin-top: 30px; text-align: center; }
                    </style>
                </head>
                <body>
                    <h2 class="header">Ministère de la Défense Nationale</h2>
                    
                    <h3>Bonjour %s %s,</h3>
                    
                    <p>Nous avons bien reçu votre candidature pour l'unité <strong>%s</strong>.</p>
                    
                    <p>Votre dossier est en cours d'examen. Vous pouvez suivre son état en vous connectant :</p>
                    
                    <p style="text-align: center;">
                        <a href="%s/mon-compte" class="button">Consulter mon dossier</a>
                    </p>
                    
                    <div class="recap">
                        <h4>Récapitulatif :</h4>
                        <ul>
                            <li><strong>CIN :</strong> %s</li>
                            <li><strong>Email :</strong> %s</li>
                            <li><strong>Téléphone :</strong> %s</li>
                            <li><strong>Diplôme :</strong> %s</li>
                            <li><strong>Établissement :</strong> %s</li>
                        </ul>
                    </div>
                    
                    <p class="footer">
                        Cordialement,<br>
                        Direction des Ressources Humaines<br>
                        Ministère de la Défense Nationale<br>
                        %s
                    </p>
                </body>
                </html>
                """.formatted(
                escapeHtml(c.getPrenom()), escapeHtml(c.getNom()),
                unite,
                frontendUrl,
                escapeHtml(c.getCin()), escapeHtml(c.getEmail()), escapeHtml(c.getTelephone()),
                escapeHtml(c.getDiplome()), escapeHtml(c.getEtablissement()),
                java.time.LocalDate.now().toString()
        );
    }

    private String buildRHNotificationContent(Candidature c) {
        String unite = c.getUniteChoisie() != null ? escapeHtml(c.getUniteChoisie().getNom()) : "non spécifiée";

        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Nouvelle candidature reçue</h2>
                    <p><strong>Candidat :</strong> %s %s</p>
                    <p><strong>CIN :</strong> %s</p>
                    <p><strong>Email :</strong> %s</p>
                    <p><strong>Téléphone :</strong> %s</p>
                    <p><strong>Diplôme :</strong> %s</p>
                    <p><strong>Établissement :</strong> %s</p>
                    <p><strong>Unité souhaitée :</strong> %s</p>
                    <p><strong>ID candidature :</strong> %d</p>
                    <p style="margin-top: 20px;">
                        <a href="%s/admin/candidatures/%d" style="color: #1e3a5f; font-weight: bold;">Voir le dossier complet</a>
                    </p>
                    <p style="font-size: 14px; color: #666; margin-top: 30px;">
                        Cordialement,<br>
                        Système de recrutement
                    </p>
                </body>
                </html>
                """.formatted(
                escapeHtml(c.getPrenom()), escapeHtml(c.getNom()),
                escapeHtml(c.getCin()), escapeHtml(c.getEmail()), escapeHtml(c.getTelephone()),
                escapeHtml(c.getDiplome()), escapeHtml(c.getEtablissement()),
                unite, c.getId(),
                frontendUrl, c.getId()
        );
    }

    private String buildStatutUpdateContent(Candidature c, String commentaire) {
        String statutText = switch (c.getStatut()) {
            case VALIDEE -> "<strong style='color: #22c55e;'>acceptée</strong>";
            case REJETEE -> "<strong style='color: #ef4444;'>rejetée</strong>";
            case EN_EXAMEN -> "<strong style='color: #3b82f6;'>en cours d'examen</strong>";
            default -> "mise à jour";
        };

        String unite = c.getUniteChoisie() != null ? escapeHtml(c.getUniteChoisie().getNom()) : "non spécifiée";

        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Mise à jour de votre candidature</h2>
                    <p>Votre candidature pour l'unité <strong>%s</strong> a été %s.</p>
                    %s
                    <p>Consultez les détails dans votre espace personnel :</p>
                    <p style="text-align: center;">
                        <a href="%s/mon-compte" style="background-color: #1e3a5f; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Mon compte
                        </a>
                    </p>
                    <p style="font-size: 14px; color: #666; margin-top: 30px;">
                        Cordialement,<br>
                        Ministère de la Défense Nationale
                    </p>
                </body>
                </html>
                """.formatted(
                unite,
                statutText,
                commentaire != null && !commentaire.isEmpty() ? "<p><strong>Motif :</strong> " + escapeHtml(commentaire) + "</p>" : "",
                frontendUrl
        );
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}