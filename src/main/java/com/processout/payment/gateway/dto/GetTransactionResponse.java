package com.processout.payment.gateway.dto;

import com.processout.payment.gateway.model.CardDetails;
import com.processout.payment.gateway.model.CurrencyEnum;
import com.processout.payment.gateway.model.TransactionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class GetTransactionResponse {
    private Long transactionId;
    private Double amount;
    private Date submissionDate;
    private Date transactionDate;
    private TransactionStatus status;
    private String declineReason;
    private CurrencyEnum currency;
    private CardDetails cardDetails;
}
