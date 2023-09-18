package com.processout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "transactions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Valid
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    @NotNull
    private Merchant merchant;

    @Column(name = "amount")
    @NotNull
    @DecimalMin(value = "0.0")
    private Double amount;

    @Column(name = "submission_date")
    @NotNull
    private Date submissionDate;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "status")
    @NotNull
    private TransactionStatus status;

    @Column(name = "declineReason")
    private String declineReason;

    @Column(name = "currency")
    @NotNull
    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;

    @Embedded
    @JsonProperty("cardDetails")
    @Valid
    private CardDetails cardDetails;
}

