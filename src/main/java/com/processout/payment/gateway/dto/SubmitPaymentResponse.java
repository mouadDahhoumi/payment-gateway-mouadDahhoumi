package com.processout.payment.gateway.dto;

import com.processout.payment.gateway.model.TransactionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SubmitPaymentResponse {
    private Long transactionId;
    private TransactionStatus status;
}
