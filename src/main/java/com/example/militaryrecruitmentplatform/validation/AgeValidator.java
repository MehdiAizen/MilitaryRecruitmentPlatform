package com.example.militaryrecruitmentplatform.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
        this.maxAge = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(LocalDate dateNaissance, ConstraintValidatorContext context) {
        if (dateNaissance == null) {
            return false;
        }
        int age = Period.between(dateNaissance, LocalDate.now()).getYears();
        return age >= minAge && age <= maxAge;
    }
}