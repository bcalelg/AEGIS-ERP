package com.aegis.erp.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        LocalDate today = LocalDate.now(context.getClockProvider().getClock());
        return !value.isAfter(today.minusYears(18));
    }
}
