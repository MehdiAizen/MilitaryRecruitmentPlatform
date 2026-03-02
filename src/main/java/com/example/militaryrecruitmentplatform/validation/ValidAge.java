package com.example.militaryrecruitmentplatform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAge {
    String message() default "L'âge du candidat doit être compris entre 18 et 35 ans";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int min() default 18;
    int max() default 35;
}