package com.processout.payment.gateway.exceptions;

public class MerchantNotFoundException extends RuntimeException {

    public MerchantNotFoundException() {
        super();
    }

    public MerchantNotFoundException(String message) {
        super(message);
    }

    public MerchantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
