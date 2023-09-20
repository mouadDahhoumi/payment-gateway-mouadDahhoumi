package com.processout.payment.gateway.dto;

import com.processout.payment.gateway.model.CurrencyEnum;
import com.processout.payment.gateway.validation.ValidCardNumber;
import com.processout.payment.gateway.validation.ValidCreditCardDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Valid
@ValidCreditCardDate
public class SubmitPaymentRequestBody {
    @NotNull
    private Long merchantId;

    @NotNull
    @DecimalMin(value = "0.0")
    private Double amount;

    @NotNull
    private CurrencyEnum currency;

    @Min(1)
    @Max(12)
    private Integer expiryMonth;

    @Min(2022)
    @Max(5000)
    private Integer expiryYear;

    @NotBlank
    private String owner;

    @NotBlank
    @ValidCardNumber
    private String cardNumber;

    @NotNull
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CCV must be a three or four digits number")
    private String ccv;
}
