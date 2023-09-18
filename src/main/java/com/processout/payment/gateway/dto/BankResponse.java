package com.processout.payment.gateway.dto;

import com.processout.payment.gateway.model.TransactionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
public class BankResponse {
    public static final String CANCELED_CARD = "Canceled card";
    public static final String EXPIRED_CARD = "Expired card";
    public static final String LACK_OF_FUND = "Lack of fund";
    public static final String INCORRECT_PAYMENT_INFORMATION = "Incorrect payment information";
    public static final String UNVERIFIED_CUSTOMER = "Unverified customer";
    public static final String NETWORK_FAILURE = "Network failures or timeouts";
    public static final List<String> DECLINE_REASONS = List.of(CANCELED_CARD, EXPIRED_CARD, LACK_OF_FUND, //
            INCORRECT_PAYMENT_INFORMATION, UNVERIFIED_CUSTOMER);

    private TransactionStatus status;
    private Date operationDate;
    private String declineReason;
}
