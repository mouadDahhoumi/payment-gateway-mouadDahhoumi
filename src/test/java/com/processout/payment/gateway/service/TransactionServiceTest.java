package com.processout.payment.gateway.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import com.processout.payment.gateway.exceptions.MerchantNotFoundException;
import com.processout.payment.gateway.model.Merchant;
import com.processout.payment.gateway.model.Transaction;
import com.processout.payment.gateway.repository.MerchantRepository;
import com.processout.payment.gateway.repository.TransactionRepository;

import com.processout.payment.gateway.service.impl.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void testGetAllTransactions() {
        // Arrange
        List<Transaction> transactions = List.of(new Transaction());
        when(transactionRepository.findAll()).thenReturn(transactions);

        // Act
        List<Transaction> result = transactionService.getAllTransactions();

        // Assert
        assertEquals(transactions, result);
    }

    @Test
    public void testGetTransactionById() {
        // Arrange
        Long transactionId = 1L;
        Transaction expectedTransaction = new Transaction();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(expectedTransaction));

        // Act
        Transaction result = transactionService.getTransactionById(transactionId);

        // Assert
        assertEquals(expectedTransaction, result);
    }

    // You can similarly write tests for other methods

    @Test
    public void testSaveTransactionWithValidMerchant() {
        // Arrange
        Transaction transaction = new Transaction();
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        transaction.setMerchant(merchant);
        when(merchantRepository.findById(merchant.getId())).thenReturn(Optional.of(merchant));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        Transaction result = transactionService.saveTransaction(transaction);

        // Assert
        assertEquals(transaction, result);
    }

    @Test
    public void testSaveTransactionWithInvalidMerchant() {
        // Arrange
        Transaction transaction = new Transaction();
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        transaction.setMerchant(merchant);
        when(merchantRepository.findById(merchant.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MerchantNotFoundException.class, () -> transactionService.saveTransaction(transaction));
    }
}

