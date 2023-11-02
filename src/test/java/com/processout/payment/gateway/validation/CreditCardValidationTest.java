package com.processout.payment.gateway.validation;

import org.junit.Test;

import java.time.LocalDate;

import static com.processout.payment.gateway.validation.CreditCardDateValidator.isCardExpired;
import static com.processout.payment.gateway.validation.CreditCardNumberValidator.isValidCardNumber;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreditCardValidationTest {
    @Test
    public void testValidCardNumber() {
        assertTrue(isValidCardNumber("4532 0151 1283 0366"));
    }

    @Test
    public void testValidCardNumberWithSpaces() {
        assertTrue(isValidCardNumber("4 5320 1511 2830 366"));
    }

    @Test
    public void testInvalidCardNumber() {
        assertFalse(isValidCardNumber("4532015112830367"));
    }

    @Test
    public void testInvalidCardNumberWithSpaces() {
        assertFalse(isValidCardNumber("4 5320 1511 2830 367"));
    }

    @Test
    public void testEmptyCardNumber() {
        assertFalse(isValidCardNumber(""));
    }

    @Test
    public void testNonNumericCharacters() {
        assertFalse(isValidCardNumber("45320151A2830366"));
    }

    @Test
    public void testNullCardNumber() {
        assertFalse(isValidCardNumber(null));
    }

    @Test
    public void testCardNotExpired() {
        // Set the expiration date to a future date
        int expirationMonth = LocalDate.now().getMonthValue() + 1;
        int expirationYear = LocalDate.now().getYear() + 1;

        assertFalse(isCardExpired(expirationMonth, expirationYear));
    }

    @Test
    public void testCardExpiredThisMonth() {
        // Set the expiration date to the current month and year
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        assertFalse(isCardExpired(currentMonth, currentYear));
    }

    @Test
    public void testCardExpiredLastMonth() {
        // Set the expiration date to the previous month
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        int expirationMonth = currentMonth - 1;
        int expirationYear = currentYear;

        assertTrue(isCardExpired(expirationMonth, expirationYear));
    }

    @Test
    public void testCardExpiredLastYear() {
        // Set the expiration date to the previous year
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        int expirationMonth = currentMonth;
        int expirationYear = currentYear - 1;

        assertTrue(isCardExpired(expirationMonth, expirationYear));
    }
}
