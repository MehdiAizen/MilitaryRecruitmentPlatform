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

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Pointcut("execution(* com.example.militaryrecruitmentplatform.service.*.*(..))")
    public void serviceMethods() {}

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";

        log.info("[AUDIT] User: {} | Method: {} | Args: {} | Time: {} | Result: {}",
                username,
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()),
                LocalDateTime.now(),
                result != null ? "success" : "null"
        );
    }
}