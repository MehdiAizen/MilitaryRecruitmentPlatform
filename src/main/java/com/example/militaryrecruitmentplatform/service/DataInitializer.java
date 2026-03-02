package com.example.militaryrecruitmentplatform.service;

import com.example.militaryrecruitmentplatform.model.Role;
import com.example.militaryrecruitmentplatform.model.Unite;
import com.example.militaryrecruitmentplatform.model.User;
import com.example.militaryrecruitmentplatform.repository.UniteRepository;
import com.example.militaryrecruitmentplatform.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final UniteRepository uniteRepository;
    private final PasswordEncoder passwordEncoder;

    // FIX: charger les mots de passe depuis les variables d'environnement
    // Valeurs par défaut conformes à la politique : 8+ chars, maj, min, chiffre, spécial
    // NE PAS utiliser ces valeurs par défaut en production !
    @Value("${app.init.admin-password:Admin@2025!}")
    private String adminPassword;

    @Value("${app.init.rh-password:Rh@2025!Trabelsi}")
    private String rhPassword;

    @Value("${app.init.cmd-password:Cmd@2025!Jebali}")
    private String cmdPassword;

    @PostConstruct
    public void init() {
        initUsers();
        initUnites();
    }

    private void initUsers() {
        if (userRepository.existsByEmail("admin@defense-test.tn")) {
            log.info("Utilisateurs déjà initialisés — skip");
            return;
        }

        // FIX: mots de passe conformes à la politique de sécurité (8+ cars, maj, min, chiffre, spécial)
        // Avant : admin123, rh123, cmd123 — trop faibles et non conformes à la regex de UserRequest
        createUser("admin@defense-test.tn", adminPassword, "Admin", "System", Role.ADMIN);
        createUser("rh@defense.tn", rhPassword, "Trabelsi", "Salma", Role.RH);
        createUser("cmd@defense-test.tn", cmdPassword, "Jebali", "Karim", Role.COMMANDANT);

        log.info("Comptes de démo créés. ATTENTION : changer les mots de passe en production !");
    }

    private void createUser(String email, String password, String nom, String prenom, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void initUnites() {
        if (uniteRepository.count() > 0) {
            log.info("Unités déjà initialisées — skip");
            return;
        }

        List<Unite> unites = List.of(
                makeUnite("Brigade Blindée de Sfax",        "Sfax",      "Blindée"),
                makeUnite("Régiment d'Infanterie de Tunis", "Tunis",     "Infanterie"),
                makeUnite("Bataillon du Génie de Sousse",   "Sousse",    "Génie"),
                makeUnite("Garde Nationale de Bizerte",     "Bizerte",   "Garde"),
                makeUnite("Unité Spéciale de Gabès",        "Gabès",     "Spéciale"),
                makeUnite("Bataillon Logistique de Nabeul", "Nabeul",    "Logistique"),
                makeUnite("Régiment d'Artillerie de Béja",  "Béja",      "Artillerie"),
                makeUnite("Brigade Aéroportée de Kasserine","Kasserine", "Aéroportée"),
                makeUnite("Unité Navale de La Goulette",    "Tunis",     "Marine"),
                makeUnite("Bataillon des Transmissions",    "Tunis",     "Transmissions")
        );

        uniteRepository.saveAll(unites);
        log.info("{} unités militaires créées", unites.size());
    }

    private Unite makeUnite(String nom, String gouvernorat, String type) {
        Unite u = new Unite();
        u.setNom(nom);
        u.setGouvernorat(gouvernorat);
        u.setType(type);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }
}