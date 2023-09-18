package com.processout.payment.gateway.utils;

import com.processout.payment.gateway.model.CardDetails;
import com.processout.payment.gateway.model.Transaction;

import java.util.function.Function;

public class Utils {

    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static String maskCardNumber(String cardNumber) {
        // Check if the card number is null or empty
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "";
        }

        // Define how many digits to show at the end
        int visibleDigits = 4;

        // Calculate the number of asterisks (*) to use
        int maskLength = cardNumber.length() - visibleDigits;

        // Create the masked card number
        StringBuilder maskedCardNumber = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            maskedCardNumber.append('*');
        }

        // Append the last visible digits
        maskedCardNumber.append(cardNumber.substring(maskLength));

        return maskedCardNumber.toString();
    }

    public static Function<Transaction, Transaction> maskCardNumber() {
        return transaction -> {
            CardDetails cardDetails = transaction.getCardDetails();
            cardDetails.setCardNumber(Utils.maskCardNumber(cardDetails.getCardNumber()));
            transaction.setCardDetails(cardDetails);
            return transaction;
        };
    }

    public static Transaction maskCardNumber(Transaction transaction) {
        if (transaction != null) {
            CardDetails cardDetails = transaction.getCardDetails();
            cardDetails.setCardNumber(Utils.maskCardNumber(cardDetails.getCardNumber()));
            transaction.setCardDetails(cardDetails);
        }

        return transaction;
    }
}
