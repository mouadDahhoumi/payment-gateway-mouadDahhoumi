package com.processout.payment.gateway.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class CreditCardNumberValidator implements ConstraintValidator<ValidCardNumber, String> {
    public boolean isValid(String cardNumber, ConstraintValidatorContext c){
        return isValidCardNumber(cardNumber);
    }


    // Luhn Algorithm
    public static boolean isValidCardNumber(String cardNo) {
        if (StringUtils.isBlank(cardNo)) return false;

        cardNo = cardNo.replaceAll("\\s", "");
        int nDigits = cardNo.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--) {

            int d = cardNo.charAt(i) - '0';

            if (isSecond == true) d = d * 2;

            // We add two digits to handle
            // cases that make two digits
            // after doubling
            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }

        return (nSum % 10 == 0);
    }
}
