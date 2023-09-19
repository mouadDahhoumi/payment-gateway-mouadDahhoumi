package com.processout.payment.gateway.controller;

import com.processout.payment.gateway.dto.GetTransactionResponse;
import com.processout.payment.gateway.dto.MerchantIdRequestBody;
import com.processout.payment.gateway.dto.SubmitPaymentRequestBody;
import com.processout.payment.gateway.dto.SubmitPaymentResponse;
import com.processout.payment.gateway.model.*;
import com.processout.payment.gateway.service.IMerchantService;
import com.processout.payment.gateway.service.ITransactionService;

import com.processout.payment.gateway.utils.Utils;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping()
    public ResponseEntity<SubmitPaymentResponse> submitTransaction(@RequestBody @Valid SubmitPaymentRequestBody body) {
        Merchant merchant = merchantService.getMerchantById(body.getMerchantId());
        if (merchant == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CardDetails cardDetails = Utils.extractCardDetails(body);
        Transaction submittedTransaction = Utils.mapSubmitRequestBodyToTransaction(body, merchant, cardDetails);
        Transaction savedTransaction = transactionService.saveTransaction(submittedTransaction);
        rabbitTemplate.convertAndSend(topic, savedTransaction);
        return ResponseEntity.ok(SubmitPaymentResponse.builder().transactionId(savedTransaction.getId()).status(savedTransaction.getStatus()).build());
    }

    @GetMapping()
    public ResponseEntity<List<GetTransactionResponse>> getTransactionsByMerchantId(@RequestBody @Valid MerchantIdRequestBody merchant) {
        List<Transaction> transactions = transactionService.findAllTransactionsByMerchantId(merchant.getMerchantId());
        if (transactions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transactions.stream().map(Utils::mapTransactionToGetTransactionResponse).toList());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<GetTransactionResponse> getTransactionByIdAndMerchantId(@RequestBody @Valid MerchantIdRequestBody merchant, @PathVariable Long transactionId) {
        Transaction transaction = transactionService.findTransactionByIdAndMerchantId(transactionId, merchant.getMerchantId());
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }

        GetTransactionResponse getTransactionResponse = Utils.mapTransactionToGetTransactionResponse(transaction);
        return ResponseEntity.ok(getTransactionResponse);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<GetTransactionResponse>> getAllPendingTransactions(@RequestBody @Valid MerchantIdRequestBody merchant) {
        Long merchantId = merchant.getMerchantId();
        List<GetTransactionResponse> pendingTransactions = transactionService.getAllPendingTransactions(merchantId).stream().map(Utils::mapTransactionToGetTransactionResponse).toList();
        return ResponseEntity.ok(pendingTransactions);
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<GetTransactionResponse>> getAllAcceptedTransactions(@RequestBody @Valid MerchantIdRequestBody merchant) {
        Long merchantId = merchant.getMerchantId();
        List<GetTransactionResponse> pendingTransactions = transactionService.getAllAcceptedTransactions(merchantId).stream().map(Utils::mapTransactionToGetTransactionResponse).toList();
        return ResponseEntity.ok(pendingTransactions);
    }

    @GetMapping("/declined")
    public ResponseEntity<List<GetTransactionResponse>> getAllDeclinedTransactions(@RequestBody @Valid MerchantIdRequestBody merchant) {
        Long merchantId = merchant.getMerchantId();
        List<GetTransactionResponse> pendingTransactions = transactionService.getAllDeclinedTransactions(merchantId).stream().map(Utils::mapTransactionToGetTransactionResponse).toList();
        ;
        return ResponseEntity.ok(pendingTransactions);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Double> getRevenue(@RequestBody @Valid MerchantIdRequestBody merchant) {
        return ResponseEntity.ok(transactionService.calculateRevenue(merchant.getMerchantId()));
    }

    @GetMapping("/bank-approval-rate")
    public ResponseEntity<Double> getBankApprovalRate(@RequestBody @Valid MerchantIdRequestBody merchant) {
        return ResponseEntity.ok(transactionService.calculateApprovalRate(merchant.getMerchantId()));
    }
}
