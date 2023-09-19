package com.processout.payment.gateway.service.impl;

import com.processout.bank.service.IBank;
import com.processout.payment.gateway.dto.BankResponse;
import com.processout.payment.gateway.model.Transaction;
import com.processout.payment.gateway.service.IPaymentProcessor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.processout.payment.gateway.dto.BankResponse.NETWORK_FAILURE;
import static com.processout.payment.gateway.model.TransactionStatus.ACCEPTED;
import static com.processout.payment.gateway.model.TransactionStatus.DECLINED;

@Service
public class PaymentProcessor implements IPaymentProcessor {
    private final IBank mockBank;
    private final TransactionService transactionService;
    private final Logger logger = LogManager.getLogger(PaymentProcessor.class);

    @Value("${payment.processor.max.retries}")
    public int MAX_RETRIES;

    public PaymentProcessor(IBank mockBank, TransactionService transactionService) {
        this.mockBank = mockBank;
        this.transactionService = transactionService;
    }

    @Override
    @RabbitListener(queues = {"${spring.rabbitmq.topic}"}, concurrency = "3")
    public void processPayment(Transaction transaction) {
        int currentAttempt = 0;
        boolean success = false;
        BankResponse bankResponse = BankResponse.builder().build();
        while (currentAttempt < MAX_RETRIES && !success) {
            logger.info(String.format("Processing started for transaction with id: %d", transaction.getId()));
            bankResponse = mockBank.processTransaction(transaction);
            if (bankResponse.getStatus() == ACCEPTED) {
                logger.info(String.format("Transaction with id %d succeeded. Attempt no: %d", transaction.getId(), currentAttempt + 1));
                success = true;
            } else if (bankResponse.getStatus() == DECLINED && bankResponse.getDeclineReason().equals(NETWORK_FAILURE)) {
                logger.info(String.format("Transaction with id %d failed due to a network failure. Retrying ...", transaction.getId()));
            } else {
                logger.info(String.format("Transaction with id %d failed due to %s.", transaction.getId(), bankResponse.getDeclineReason()));
                break;
            }

            currentAttempt++;
        }

        if (!success && bankResponse.getDeclineReason().equals(NETWORK_FAILURE)) logger.info(String.format("Maximum retries reached. Operation failed for transaction with id: %d", transaction.getId()));
        transactionService.updateTransaction(transaction.getId(), bankResponse.getStatus(), bankResponse.getOperationDate(), bankResponse.getDeclineReason());
        logger.info(String.format("Processing ended for transaction with id: %d", transaction.getId()));
    }
}
