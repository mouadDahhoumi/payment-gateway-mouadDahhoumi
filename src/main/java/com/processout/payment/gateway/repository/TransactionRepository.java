package com.processout.payment.gateway.repository;

import com.processout.payment.gateway.model.Transaction;
import com.processout.payment.gateway.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.merchant.id = ?1")
    List<Transaction> findAllByMerchantId(Long merchantId);

    List<Transaction> findByMerchantIdAndStatus(Long merchantId, TransactionStatus status);
    Optional<Transaction> findByIdAndMerchantId(Long transactionId, Long merchantId);
}

