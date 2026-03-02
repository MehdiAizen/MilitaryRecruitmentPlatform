package com.example.militaryrecruitmentplatform.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    // FIX: liste des méthodes sensibles dont on ne doit PAS logger les arguments
    private static final java.util.Set<String> SENSITIVE_METHODS = java.util.Set.of(
            "login", "register", "createUser", "updateUser", "changePassword"
    );

    @Pointcut("execution(* com.example.militaryrecruitmentplatform.service.*.*(..))")
    public void serviceMethods() {}

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";

        String methodName = joinPoint.getSignature().getName();

        // FIX: ne pas logger les arguments des méthodes sensibles (mots de passe en clair)
        String argsLog = SENSITIVE_METHODS.contains(methodName)
                ? "[ARGUMENTS MASQUÉS - MÉTHODE SENSIBLE]"
                : maskSensitiveArgs(joinPoint.getArgs());

        log.info("[AUDIT] User: {} | Method: {} | Args: {} | Time: {} | Result: {}",
                username,
                methodName,
                argsLog,
                LocalDateTime.now(),
                result != null ? "success" : "null"
        );
    }

    // FIX: masque les champs potentiellement sensibles dans les arguments
    private String maskSensitiveArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    String str = arg.toString();
                    // Masquer si l'objet contient "password" dans sa représentation
                    if (str.toLowerCase().contains("password")) {
                        return "[OBJET AVEC CHAMPS SENSIBLES]";
                    }
                    return str;
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }
}