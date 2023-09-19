package com.processout.payment.gateway.utils;

import com.processout.payment.gateway.dto.GetTransactionResponse;
import com.processout.payment.gateway.dto.SubmitPaymentRequestBody;
import com.processout.payment.gateway.model.CardDetails;
import com.processout.payment.gateway.model.Merchant;
import com.processout.payment.gateway.model.Transaction;

import java.util.Date;
import java.util.function.Function;

import static com.processout.payment.gateway.model.TransactionStatus.PENDING;

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

    public static CardDetails extractCardDetails(SubmitPaymentRequestBody body) {
        return CardDetails.builder().ccv(body.getCcv()).cardNumber(body.getCardNumber()).expiryMonth(body.getExpiryMonth()).expiryYear(body.getExpiryYear()).owner(body.getOwner()).build();
    }

    public static GetTransactionResponse mapTransactionToGetTransactionResponse(Transaction transaction) {
        return GetTransactionResponse.builder().transactionId(transaction.getId()).amount(transaction.getAmount()).currency(transaction.getCurrency()).declineReason(transaction.getDeclineReason()).transactionDate(transaction.getTransactionDate()).submissionDate(transaction.getSubmissionDate()).status(transaction.getStatus()).cardDetails(transaction.getCardDetails()).build();
    }

    public static Transaction mapSubmitRequestBodyToTransaction(SubmitPaymentRequestBody body, Merchant merchant, CardDetails cardDetails) {
        return Transaction.builder().amount(body.getAmount()).merchant(merchant).cardDetails(cardDetails).status(PENDING).submissionDate(new Date()).currency(body.getCurrency()).build();
    }
}
