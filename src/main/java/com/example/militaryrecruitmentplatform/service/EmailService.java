package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.model.Candidature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envoie un email de confirmation au candidat
     */
    public void sendCandidatConfirmation(Candidature candidature) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(candidature.getEmail());
            helper.setSubject("Candidature reçue - Ministère de la Défense Nationale");

            String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;">
                        <div style="text-align: center; margin-bottom: 20px;">
                            <h2 style="color: #1e3a5f;">Ministère de la Défense Nationale</h2>
                        </div>
                        
                        <h3 style="color: #1e3a5f;">Bonjour %s %s,</h3>
                        
                        <p>Nous accusons réception de votre candidature pour l'unité <strong>%s</strong>.</p>
                        
                        <p>Votre dossier sera examiné par notre équipe dans les plus brefs délais. 
                        Vous recevrez une notification par email concernant l'avancement de votre candidature.</p>
                        
                        <div style="background-color: #f0f0f0; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h4 style="margin-top: 0;">Récapitulatif de votre candidature :</h4>
                            <ul style="list-style: none; padding: 0;">
                                <li><strong>CIN :</strong> %s</li>
                                <li><strong>Email :</strong> %s</li>
                                <li><strong>Téléphone :</strong> %s</li>
                                <li><strong>Diplôme :</strong> %s</li>
                                <li><strong>Établissement :</strong> %s</li>
                                <li><strong>Unité choisie :</strong> %s</li>
                            </ul>
                        </div>
                        
                        <p style="color: #666; font-size: 14px; margin-top: 30px;">
                            Cordialement,<br>
                            <strong>Direction des Ressources Humaines</strong><br>
                            Ministère de la Défense Nationale
                        </p>
                    </div>
                </body>
                </html>
                """,
                    candidature.getPrenom(),
                    candidature.getNom(),
                    candidature.getUniteChoisie(),
                    candidature.getCin(),
                    candidature.getEmail(),
                    candidature.getTelephone(),
                    candidature.getDiplome(),
                    candidature.getEtablissement(),
                    candidature.getUniteChoisie()
            );

            helper.setText(content, true);
            mailSender.send(message);

            log.info("Email de confirmation envoyé à {} pour candidature ID {}",
                    candidature.getEmail(), candidature.getId());
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation: {}", e.getMessage());
        }
    }

    /**
     * Envoie une notification à l'équipe RH
     */
    public void sendRHNotification(Candidature candidature) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Remplace par l'email RH réel ou utilise une variable d'environnement
            helper.setTo("rh@defense.tn");
            helper.setSubject("Nouvelle candidature - " + candidature.getUniteChoisie());

            String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #1e3a5f;">Nouvelle candidature reçue</h2>
                        
                        <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                            <tr style="background-color: #f0f0f0;">
                                <td style="padding: 10px; border: 1px solid #ddd;"><strong>Candidat</strong></td>
                                <td style="padding: 10px; border: 1px solid #ddd;">%s %s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border: 1px solid #ddd;"><strong>CIN</strong></td>
                                <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr style="background-color: #f0f0f0;">
                                <td style="padding: 10px; border: 1px solid #ddd;"><strong>Email</strong></td>
                                <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border: 1px solid #ddd;"><strong>Téléphone</strong></td>
                                <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr style="background-color: #f0f0f0;">
                                <td style="padding: 10px; border: 1px solid #ddd;"><strong>Diplôme</strong></td>
                                <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border: 1px solid #ddd;"><strong>Établissement</strong></td>
                                <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                            </tr>
                            <tr style="background-color: #f0f0f0;">
                                <td style="padding: 10px; border: 1px solid #ddd;"><strong>Unité choisie</strong></td>
                                <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                            </tr>
                        </table>
                        
                        <p style="margin-top: 20px;">
                            <a href="http://localhost:3000/candidatures" 
                               style="display: inline-block; padding: 10px 20px; background-color: #1e3a5f; 
                                      color: white; text-decoration: none; border-radius: 5px;">
                                Voir la candidature
                            </a>
                        </p>
                    </div>
                </body>
                </html>
                """,
                    candidature.getPrenom(),
                    candidature.getNom(),
                    candidature.getCin(),
                    candidature.getEmail(),
                    candidature.getTelephone(),
                    candidature.getDiplome(),
                    candidature.getEtablissement(),
                    candidature.getUniteChoisie()
            );

            helper.setText(content, true);
            mailSender.send(message);

            log.info("Notification RH envoyée pour candidature ID {}", candidature.getId());
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la notification RH: {}", e.getMessage());
        }
    }

    /**
     * Envoie un email de mise à jour du statut
     */
    public void sendStatutUpdate(Candidature candidature, String commentaire) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(candidature.getEmail());
            helper.setSubject("Mise à jour de votre candidature - " + candidature.getUniteChoisie());

            String messageStatut = switch (candidature.getStatut()) {
                case VALIDEE -> "Félicitations ! Votre candidature a été <strong style='color: #22c55e;'>acceptée</strong>.";
                case REJETEE -> "Nous sommes au regret de vous informer que votre candidature n'a pas été retenue.";
                case EN_EXAMEN -> "Votre candidature est actuellement <strong style='color: #3b82f6;'>en cours d'examen</strong>.";
                default -> "Le statut de votre candidature a été mis à jour.";
            };

            String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;">
                        <h2 style="color: #1e3a5f;">Bonjour %s %s,</h2>
                        
                        <p>%s</p>
                        
                        <div style="background-color: #f0f0f0; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Unité :</strong> %s</p>
                            <p><strong>Statut :</strong> %s</p>
                            %s
                        </div>
                        
                        <p style="color: #666; font-size: 14px; margin-top: 30px;">
                            Cordialement,<br>
                            <strong>Direction des Ressources Humaines</strong><br>
                            Ministère de la Défense Nationale
                        </p>
                    </div>
                </body>
                </html>
                """,
                    candidature.getPrenom(),
                    candidature.getNom(),
                    messageStatut,
                    candidature.getUniteChoisie(),
                    candidature.getStatut(),
                    commentaire != null && !commentaire.isEmpty()
                            ? "<p><strong>Commentaire :</strong> " + commentaire + "</p>"
                            : ""
            );

            helper.setText(content, true);
            mailSender.send(message);

            log.info("Email de mise à jour de statut envoyé à {} pour candidature ID {}",
                    candidature.getEmail(), candidature.getId());
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de mise à jour: {}", e.getMessage());
        }
    }
}