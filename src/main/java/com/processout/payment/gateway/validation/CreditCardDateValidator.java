package com.processout.payment.gateway.validation;

import com.processout.payment.gateway.dto.SubmitPaymentRequestBody;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class CreditCardDateValidator implements ConstraintValidator<ValidCardDate, SubmitPaymentRequestBody> {
    @Override
    public boolean isValid(SubmitPaymentRequestBody card, ConstraintValidatorContext constraintValidatorContext) {
        return !isCardExpired(card.getExpiryMonth(), card.getExpiryYear());
    }

    public static boolean isCardExpired(int expirationMonth, int expirationYear) {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Extract the current year and month
        int currentYearNow = currentDate.getYear();
        int currentMonthNow = currentDate.getMonthValue();

        // Return whether the card has already expired
        return currentYearNow > expirationYear || (currentYearNow == expirationYear && currentMonthNow > expirationMonth); // Card has expired
    }
}
