package com.processout.payment.gateway.service;

import com.processout.payment.gateway.model.Transaction;

public interface IPaymentProcessor {
    public void processPayment(Transaction transaction);
}
