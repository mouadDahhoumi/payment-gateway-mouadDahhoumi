package com.processout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CurrencyEnum {
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    JPY("JPY"),
    CAD("CAD"),
    AUD("AUD");

    private final String currency;

    CurrencyEnum(String currency) {
        this.currency = currency;
    }


    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return currency;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CurrencyEnum fromText(String text) {
        for (CurrencyEnum r : CurrencyEnum.values()) {
            if (r.getCurrency().equals(text)) {
                return r;
            }
        }
        throw new IllegalArgumentException();
    }
}
