package com.processout.bank.service;

import com.processout.payment.gateway.dto.BankResponse;
import com.processout.payment.gateway.model.Transaction;

public interface IBank {
    public BankResponse processTransaction(Transaction transaction);
}
