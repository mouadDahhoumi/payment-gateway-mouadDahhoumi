package com.processout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;

@Embeddable
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Valid
public class CardDetails implements Serializable {

    @Column(name = "expiry_month")
    @JsonProperty("expiryMonth")
    @NotNull
    @Min(1)
    @Max(12)
    private int expiryMonth;

    @Column(name = "expiry_year")
    @JsonProperty("expiryYear")
    @NotNull
    @Min(2022)
    @Max(5000)
    private int expiryYear;

    @Column(name = "owner")
    @JsonProperty("owner")
    @NotNull
    private String owner;

    @Column(name = "card_number")
    @JsonProperty("cardNumber")
    @NotNull
    private String cardNumber;

    @Column(name = "ccv")
    @JsonProperty("ccv")
    @NotNull
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CCV must be a three or four digits number")
    private String ccv;
}
