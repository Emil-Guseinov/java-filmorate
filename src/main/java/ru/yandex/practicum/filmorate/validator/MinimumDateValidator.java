package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MinimumDateValidator implements ConstraintValidator<MinimumDate, LocalDate> {
    private LocalDate minimumDate;

    @Override
    public void initialize(MinimumDate constraintAnnotation) {

        this.minimumDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        return !value.isBefore(minimumDate);
    }
}