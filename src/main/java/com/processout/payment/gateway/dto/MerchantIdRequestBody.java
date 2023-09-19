package com.processout.payment.gateway.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantIdRequestBody {
    @NotNull
    private Long merchantId;
}
