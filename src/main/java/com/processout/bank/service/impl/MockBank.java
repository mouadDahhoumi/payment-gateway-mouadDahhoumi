package com.processout.bank.service.impl;

import com.processout.bank.service.IBank;
import com.processout.payment.gateway.dto.BankResponse;
import com.processout.payment.gateway.model.Transaction;
import com.processout.payment.gateway.model.TransactionStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.processout.payment.gateway.dto.BankResponse.DECLINE_REASONS;
import static com.processout.payment.gateway.model.TransactionStatus.ACCEPTED;
import static com.processout.payment.gateway.model.TransactionStatus.DECLINED;


@Service
public class MockBank implements IBank {
    private static final Random random = new Random();
    private static final List<TransactionStatus> statuses = List.of(ACCEPTED, ACCEPTED, DECLINED, DECLINED);

    @Override
    public BankResponse processTransaction(Transaction transaction) {
        try {
            Thread.sleep(random.nextInt(10000));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        TransactionStatus status = getRandom(statuses);
        if (status == ACCEPTED) {
            return BankResponse.builder().status(ACCEPTED).operationDate(new Date()).build();
        }

        String declineReason = getRandom(DECLINE_REASONS);
        return BankResponse.builder().status(DECLINED).declineReason(declineReason).operationDate(new Date()).build();
    }

    public <T> T getRandom(List<T> list) {
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }
}
