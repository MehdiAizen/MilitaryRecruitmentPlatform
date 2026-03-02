package com.example.militaryrecruitmentplatform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:/app/uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}") // 10MB par défaut
    private long maxFileSize;

    // Types MIME autorisés
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // Extensions autorisées
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");

    // Signatures de fichiers (magic numbers)
    private static final Map<String, byte[]> FILE_SIGNATURES = Map.of(
            "pdf", new byte[]{(byte)0x25, (byte)0x50, (byte)0x44, (byte)0x46}, // %PDF
            "jpg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF},
            "jpeg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF},
            "png", new byte[]{(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A}
    );

    // Patterns dangereux dans les noms de fichiers
    private static final Set<String> DANGEROUS_PATTERNS = Set.of(
            "..", "/", "\\", "%00", "%2e%2e", ":", "*", "?", "\"", "<", ">", "|"
    );

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Répertoire d'upload créé: {}", uploadPath);
            }
            // Vérifier les permissions
            if (!Files.isWritable(uploadPath)) {
                log.error("Le répertoire d'upload n'est pas accessible en écriture: {}", uploadPath);
                throw new RuntimeException("Répertoire d'upload non accessible");
            }
        } catch (IOException e) {
            log.error("Impossible de créer le répertoire d'upload", e);
            throw new RuntimeException("Erreur initialisation stockage fichiers", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        // 1. Vérifier que le fichier n'est pas vide
        if (file == null || file.isEmpty()) {
            throw new IOException("Le fichier est vide");
        }

        // 2. Vérifier la taille
        if (file.getSize() > maxFileSize) {
            throw new IOException("Fichier trop volumineux. Maximum: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 3. Vérifier le nom de fichier
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IOException("Nom de fichier invalide");
        }

        // Vérifier les patterns dangereux
        String lowerFilename = originalFilename.toLowerCase();
        for (String pattern : DANGEROUS_PATTERNS) {
            if (lowerFilename.contains(pattern)) {
                log.warn("Tentative d'upload avec caractères dangereux: {}", originalFilename);
                throw new IOException("Nom de fichier contient des caractères non autorisés");
            }
        }

        // 4. Extraire et valider l'extension
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("Extension non autorisée: " + extension +
                    ". Extensions acceptées: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 5. Vérifier le Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("Content-Type non autorisé: {}", contentType);
            throw new IOException("Type de fichier non autorisé");
        }

        // Vérifier cohérence extension / content-type
        if (!isContentTypeConsistent(extension, contentType)) {
            log.warn("Incohérence extension/Content-Type: {} / {}", extension, contentType);
            throw new IOException("Type de fichier incohérent");
        }

        // 6. Lire et vérifier la signature du fichier
        byte[] fileBytes = file.getBytes();
        if (!verifyFileSignature(fileBytes, extension)) {
            log.warn("Signature de fichier invalide pour: {}", originalFilename);
            throw new IOException("Contenu de fichier invalide ou corrompu");
        }

        // 7. Vérifier contenu malveillant (PDF uniquement)
        if ("pdf".equals(extension) && containsMaliciousContent(fileBytes)) {
            log.warn("Contenu malveillant détecté dans PDF: {}", originalFilename);
            throw new IOException("Contenu de fichier non sécurisé");
        }

        // 8. Créer le répertoire si nécessaire
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // 9. Générer nom de fichier sécurisé
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        Path targetLocation = uploadPath.resolve(newFilename).normalize();

        // Vérifier que le fichier cible est bien dans le répertoire autorisé (path traversal protection)
        if (!targetLocation.startsWith(uploadPath)) {
            log.error("Tentative de path traversal détectée: {}", originalFilename);
            throw new IOException("Chemin de fichier invalide");
        }

        // 10. Sauvegarder le fichier
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 11. Définir les permissions restrictives (lecture seule)
        try {
            Files.setPosixFilePermissions(targetLocation,
                    Set.of(java.nio.file.attribute.PosixFilePermission.OWNER_READ));
        } catch (UnsupportedOperationException e) {
            // Windows ou système sans POSIX - ignorer
        }

        log.info("Fichier uploadé avec succès: {} -> {} ({} bytes)",
                originalFilename, newFilename, file.getSize());

        // FIX: On retourne l'URL complète avec le context-path /api
        // Cela permet au frontend d'utiliser l'URL directement : /api/uploads/image.png
        return "/api/uploads/" + newFilename;
    }

    /**
     * Supprime un fichier
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null) {
            throw new IOException("URL de fichier invalide");
        }

        // FIX: Gestion des deux formats d'URL (avec ou sans /api)
        String filename;
        if (fileUrl.startsWith("/api/uploads/")) {
            filename = fileUrl.substring("/api/uploads/".length());
        } else if (fileUrl.startsWith("/uploads/")) {
            filename = fileUrl.substring("/uploads/".length());
        } else {
            throw new IOException("URL de fichier invalide (format non reconnu)");
        }

        // Vérifier que le nom ne contient pas de path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IOException("Nom de fichier invalide");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(filename).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Chemin de fichier invalide");
        }

        Files.deleteIfExists(filePath);
        log.info("Fichier supprimé: {}", filePath);
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    private boolean isContentTypeConsistent(String extension, String contentType) {
        return switch (extension) {
            case "pdf" -> "application/pdf".equals(contentType);
            case "jpg", "jpeg" -> "image/jpeg".equals(contentType) || "image/jpg".equals(contentType);
            case "png" -> "image/png".equals(contentType);
            default -> false;
        };
    }

    private boolean verifyFileSignature(byte[] fileBytes, String extension) {
        byte[] expectedSignature = FILE_SIGNATURES.get(extension);
        if (expectedSignature == null || fileBytes.length < expectedSignature.length) {
            return false;
        }

        for (int i = 0; i < expectedSignature.length; i++) {
            if (fileBytes[i] != expectedSignature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean containsMaliciousContent(byte[] fileBytes) {
        // Convertir en string pour analyse (ISO-8859-1 préserve les bytes)
        String content = new String(fileBytes, StandardCharsets.ISO_8859_1).toLowerCase();

        // Patterns dangereux dans les PDF
        String[] dangerousPatterns = {
                "/javascript", "/js", "javascript:", "eval(", "alert(",
                "<script", "</script>", "onclick=", "onload=", "onerror=",
                "/openaction", "/launch", "/submitform", "/importdata"
        };

        for (String pattern : dangerousPatterns) {
            if (content.contains(pattern)) {
                log.warn("Pattern dangereux détecté dans PDF: {}", pattern);
                return true;
            }
        }

        return false;
    }
}