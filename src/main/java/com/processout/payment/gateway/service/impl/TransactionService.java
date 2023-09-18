package com.processout.payment.gateway.service.impl;

import com.processout.payment.gateway.exceptions.MerchantNotFoundException;
import com.processout.payment.gateway.model.Merchant;
import com.processout.payment.gateway.model.Transaction;
import com.processout.payment.gateway.model.TransactionStatus;
import com.processout.payment.gateway.repository.MerchantRepository;
import com.processout.payment.gateway.repository.TransactionRepository;
import com.processout.payment.gateway.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.processout.payment.gateway.model.TransactionStatus.*;
import static com.processout.payment.gateway.utils.Utils.maskCardNumber;

@Service
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;


    @Autowired
    public TransactionService(TransactionRepository transactionRepository, MerchantRepository merchantRepository) {
        this.transactionRepository = transactionRepository;
        this.merchantRepository = merchantRepository;
    }


    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Transaction> findAllTransactionsByMerchantId(Long merchantId) {
        return transactionRepository.findAllByMerchantId(merchantId) //
                .stream() //
                .map(maskCardNumber()) //
                .toList(); //
    }

    @Override
    public Transaction findTransactionByIdAndMerchantId(Long transactionId, Long merchantId) {
        Transaction transaction = transactionRepository.findByIdAndMerchantId(transactionId, merchantId).orElse(null);
        maskCardNumber(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> getAllPendingTransactions(Long merchantId) {
        return transactionRepository.findByMerchantIdAndStatus(merchantId, PENDING) //
                .stream() //
                .map(maskCardNumber()) //
                .toList(); //
    }

    @Override
    public List<Transaction> getAllDeclinedTransactions(Long merchantId) {
        return transactionRepository.findByMerchantIdAndStatus(merchantId, DECLINED).stream().map(maskCardNumber()).toList();
    }

    @Override
    public List<Transaction> getAllAcceptedTransactions(Long merchantId) {
        return transactionRepository.findByMerchantIdAndStatus(merchantId, ACCEPTED).stream().map(maskCardNumber()).toList();
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        Optional<Merchant> optionalMerchant = merchantRepository.findById(transaction.getMerchant().getId());

        if (optionalMerchant.isPresent()) {
            return transactionRepository.save(transaction);
        }

        throw new MerchantNotFoundException("Merchant not found with ID: " + transaction.getMerchant().getId());
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public Transaction updateTransaction(Long transactionId, TransactionStatus newStatus, Date operationDate, String declineReason) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);

        if (optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            transaction.setStatus(newStatus);
            transaction.setTransactionDate(operationDate);
            transaction.setDeclineReason(declineReason);
            return maskCardNumber(transactionRepository.save(transaction));
        }

        return null;
    }

    @Override
    public double calculateApprovalRate(Long merchantId) {
        return (double) getAllAcceptedTransactions(merchantId).size() / findAllTransactionsByMerchantId(merchantId).size();
    }

    @Override
    public double calculateRevenue(Long merchantId) {
        return getAllAcceptedTransactions(merchantId).stream().mapToDouble(Transaction::getAmount).sum();
    }

}