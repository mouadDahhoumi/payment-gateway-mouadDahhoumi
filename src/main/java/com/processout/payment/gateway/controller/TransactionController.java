package com.processout.payment.gateway.controller;

import com.processout.payment.gateway.dto.SubmitPaymentRequestBody;
import com.processout.payment.gateway.model.*;
import com.processout.payment.gateway.service.IMerchantService;
import com.processout.payment.gateway.service.ITransactionService;

import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static com.processout.payment.gateway.model.TransactionStatus.PENDING;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final ITransactionService transactionService;
    private final IMerchantService merchantService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.topic}")
    private String topic;

    @Autowired
    public TransactionController(ITransactionService transactionService, IMerchantService merchantService, RabbitTemplate rabbitTemplate) {
        this.transactionService = transactionService;
        this.merchantService = merchantService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/submit/{merchantId}")
    public ResponseEntity<TransactionStatus> submitTransaction(@PathVariable Long merchantId, @RequestBody @Valid SubmitPaymentRequestBody body) {
        Merchant merchant = merchantService.getMerchantById(merchantId);
        if (merchant == null || !body.getMerchantId().equals(merchantId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CardDetails cardDetails = extractCardDetails(body);
        Transaction submittedTransaction = Transaction.builder().amount(body.getAmount()).merchant(merchant).cardDetails(cardDetails).status(PENDING).submissionDate(new Date()).currency(body.getCurrency()).build();
        Transaction savedTransaction = transactionService.saveTransaction(submittedTransaction);
        rabbitTemplate.convertAndSend(topic, savedTransaction);
        return ResponseEntity.ok(submittedTransaction.getStatus());
    }


    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<Transaction>> getTransactionsByMerchantId(@PathVariable Long merchantId) {
        List<Transaction> transactions = transactionService.findAllTransactionsByMerchantId(merchantId);
        if (transactions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/merchant/{merchantId}/transaction/{transactionId}")
    public ResponseEntity<Transaction> getTransactionByIdAndMerchantId(@PathVariable Long merchantId, @PathVariable Long transactionId) {
        Transaction transaction = transactionService.findTransactionByIdAndMerchantId(transactionId, merchantId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/merchant/{merchantId}/pending")
    public ResponseEntity<List<Transaction>> getAllPendingTransactions(@PathVariable Long merchantId) {
        List<Transaction> pendingTransactions = transactionService.getAllPendingTransactions(merchantId);
        return ResponseEntity.ok(pendingTransactions);
    }

    @GetMapping("/merchant/{merchantId}/accepted")
    public ResponseEntity<List<Transaction>> getAllAcceptedTransactions(@PathVariable Long merchantId) {
        List<Transaction> pendingTransactions = transactionService.getAllAcceptedTransactions(merchantId);
        return ResponseEntity.ok(pendingTransactions);
    }

    @GetMapping("/merchant/{merchantId}/declined")
    public ResponseEntity<List<Transaction>> getAllDeclinedTransactions(@PathVariable Long merchantId) {
        List<Transaction> pendingTransactions = transactionService.getAllDeclinedTransactions(merchantId);
        return ResponseEntity.ok(pendingTransactions);
    }

    @GetMapping("/merchant/{merchantId}/revenue")
    public ResponseEntity<Double> getRevenue(@PathVariable Long merchantId) {
        return ResponseEntity.ok(transactionService.calculateRevenue(merchantId));
    }

    @GetMapping("/merchant/{merchantId}/bank-approval-rate")
    public ResponseEntity<Double> getBankApprovalRate(@PathVariable Long merchantId) {
        return ResponseEntity.ok(transactionService.calculateApprovalRate(merchantId));
    }

    private CardDetails extractCardDetails(SubmitPaymentRequestBody body) {
        return CardDetails.builder().ccv(body.getCcv()).cardNumber(body.getCardNumber()).expiryMonth(body.getExpiryMonth()).expiryYear(body.getExpiryYear()).owner(body.getOwner()).build();
    }
}
