package com.example.militaryrecruitmentplatform.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class LoginAttemptService {

    private final ConcurrentHashMap<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> blockedUntil = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> firstAttemptTime = new ConcurrentHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;
    private static final int ATTEMPT_WINDOW_MINUTES = 30;

    public void loginFailed(String ip) {
        lock.writeLock().lock();
        try {
            // Vérifier si c'est la première tentative dans la fenêtre
            LocalDateTime firstAttempt = firstAttemptTime.get(ip);
            LocalDateTime now = LocalDateTime.now();

            if (firstAttempt == null ||
                    ChronoUnit.MINUTES.between(firstAttempt, now) > ATTEMPT_WINDOW_MINUTES) {
                // Nouvelle fenêtre de tentative
                firstAttemptTime.put(ip, now);
                failedAttempts.put(ip, new AtomicInteger(1));
                log.warn("Nouvelle fenêtre de tentatives - IP: {}, tentative 1/{}", ip, MAX_ATTEMPTS);
                return;
            }

            // Incrémenter le compteur
            AtomicInteger counter = failedAttempts.computeIfAbsent(ip, k -> new AtomicInteger(0));
            int count = counter.incrementAndGet();

            log.warn("Échec login - IP: {}, tentative {}/{} dans les {} dernières minutes",
                    ip, count, MAX_ATTEMPTS, ATTEMPT_WINDOW_MINUTES);

            if (count >= MAX_ATTEMPTS) {
                LocalDateTime blockEnd = now.plusMinutes(BLOCK_DURATION_MINUTES);
                blockedUntil.put(ip, blockEnd);
                log.error("BLOCAGE IP {} jusqu'à {} ({} échecs en {} minutes)",
                        ip, blockEnd, MAX_ATTEMPTS, ATTEMPT_WINDOW_MINUTES);

                // Alert security team
                alertSecurityTeam(ip, count);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isBlocked(String ip) {
        lock.readLock().lock();
        try {
            LocalDateTime blockEnd = blockedUntil.get(ip);
            LocalDateTime now = LocalDateTime.now();

            if (blockEnd != null && now.isBefore(blockEnd)) {
                long minutesRemaining = ChronoUnit.MINUTES.between(now, blockEnd);
                log.warn("Tentative bloquée - IP: {}, déblocage dans {} minutes",
                        ip, minutesRemaining);
                return true;
            }

            // Auto-déblocage si le temps est écoulé
            if (blockEnd != null && now.isAfter(blockEnd)) {
                unblock(ip);
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Débloque manuellement une IP (pour admin)
     */
    public void unblock(String ip) {
        lock.writeLock().lock();
        try {
            blockedUntil.remove(ip);
            failedAttempts.remove(ip);
            firstAttemptTime.remove(ip);
            log.info("IP {} débloquée", ip);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void loginSucceeded(String ip) {
        lock.writeLock().lock();
        try {
            blockedUntil.remove(ip);
            failedAttempts.remove(ip);
            firstAttemptTime.remove(ip);
            log.info("Login réussi - IP {} réinitialisée", ip);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Récupère le nombre de tentatives restantes
     */
    public int getAttemptsRemaining(String ip) {
        lock.readLock().lock();
        try {
            AtomicInteger attempts = failedAttempts.get(ip);
            if (attempts == null) return MAX_ATTEMPTS;
            return Math.max(0, MAX_ATTEMPTS - attempts.get());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Récupère le temps de blocage restant en minutes
     */
    public long getBlockTimeRemaining(String ip) {
        lock.readLock().lock();
        try {
            LocalDateTime blockEnd = blockedUntil.get(ip);
            if (blockEnd == null) return 0;
            return Math.max(0, ChronoUnit.MINUTES.between(LocalDateTime.now(), blockEnd));
        } finally {
            lock.readLock().unlock();
        }
    }

    private void alertSecurityTeam(String ip, int attempts) {
        log.error("SECURITY_ALERT: Possible brute force attack - IP {} after {} failed attempts", ip, attempts);
        // Ici: envoyer alerte à Wazuh, email admin, webhook Slack, etc.
    }
}