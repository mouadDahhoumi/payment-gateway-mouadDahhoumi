package com.processout.payment.gateway.service;

import com.processout.payment.gateway.model.Transaction;
import com.processout.payment.gateway.model.TransactionStatus;

import java.util.Date;
import java.util.List;

public interface ITransactionService {
    List<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    List<Transaction> findAllTransactionsByMerchantId(Long merchantId);

    Transaction findTransactionByIdAndMerchantId(Long transactionId, Long merchantId);

    List<Transaction> getAllPendingTransactions(Long merchantId);

    List<Transaction> getAllDeclinedTransactions(Long merchantId);

    List<Transaction> getAllAcceptedTransactions(Long merchantId);

    Transaction saveTransaction(Transaction transaction);

    void deleteTransaction(Long id);

    Transaction updateTransaction(Long transactionId, TransactionStatus newStatus, Date operationDate, String declineReason);

    double calculateApprovalRate(Long merchantId);

    double calculateRevenue(Long merchantId);
}
