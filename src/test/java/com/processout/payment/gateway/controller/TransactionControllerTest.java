package com.processout.payment.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processout.payment.gateway.dto.*;
import com.processout.payment.gateway.model.*;
import com.processout.payment.gateway.service.IMerchantService;
import com.processout.payment.gateway.service.ITransactionService;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ITransactionService transactionService;

    @MockBean
    private IMerchantService merchantService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private List<Transaction> transactions;
    private Merchant merchant;
    private String validCardNumber = "4539 8319 4008 1540";
    private String invalidCardNumber = "4966 5027 3215 3333";

    @Before
    public void setUp() {
        merchant = new Merchant(1, "Merchant 1");
        CardDetails cardDetails = CardDetails.builder().owner("John Doe").expiryYear(2023).expiryMonth(12).cardNumber(validCardNumber).ccv("333").build();
        Transaction transaction = Transaction.builder().id(1L).amount(100.0).merchant(merchant).status(TransactionStatus.PENDING).submissionDate(new Date()).currency(CurrencyEnum.USD).cardDetails(cardDetails).build();
        transactions = List.of(transaction);
    }

    @Test
    public void submitTransactionWithValidFields() throws Exception {
        SubmitPaymentRequestBody requestBody = new SubmitPaymentRequestBody();
        requestBody.setAmount(100.0);
        requestBody.setMerchantId(1L);
        requestBody.setCcv("333");
        requestBody.setCardNumber(validCardNumber);
        requestBody.setExpiryMonth(12);
        requestBody.setExpiryYear(2300);
        requestBody.setOwner("John Doe");
        requestBody.setCurrency(CurrencyEnum.USD);

        when(transactionService.saveTransaction(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction submittedTransaction = invocation.getArgument(0);
            submittedTransaction.setId(2L); // Simulate saving with an ID
            submittedTransaction.setStatus(TransactionStatus.PENDING); // Simulate saving with an ID
            return submittedTransaction;
        });

        when(merchantService.getMerchantById(1L)).thenReturn(merchant);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions") //
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestBody))) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)); //

        // Validate the response
        String responseContent = result.andReturn().getResponse().getContentAsString();
        SubmitPaymentResponse response = objectMapper.readValue(responseContent, SubmitPaymentResponse.class);

        Assert.assertEquals(TransactionStatus.PENDING, response.getStatus());

        verify(transactionService, times(1)).saveTransaction(any(Transaction.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), any(Transaction.class));
    }

    @Test
    public void submitTransactionWithInvalidCardNumber() throws Exception {
        SubmitPaymentRequestBody requestBody = new SubmitPaymentRequestBody();
        requestBody.setAmount(100.0);
        requestBody.setMerchantId(1L);
        requestBody.setCcv("333");
        requestBody.setCardNumber(invalidCardNumber);
        requestBody.setExpiryMonth(12);
        requestBody.setExpiryYear(2300);
        requestBody.setOwner("John Doe");
        requestBody.setCurrency(CurrencyEnum.USD);

        when(transactionService.saveTransaction(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction submittedTransaction = invocation.getArgument(0);
            submittedTransaction.setId(2L); // Simulate saving with an ID
            submittedTransaction.setStatus(TransactionStatus.PENDING); // Simulate saving with an ID
            return submittedTransaction;
        });

        when(merchantService.getMerchantById(1L)).thenReturn(merchant);

        String error = mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions").contentType(MediaType.APPLICATION_JSON) //
                .content(objectMapper.writeValueAsString(requestBody))) //
                .andExpect(status().isBadRequest()) //
                .andReturn().getResolvedException().getMessage(); //

        assertTrue(StringUtils.contains(error, "Credit card number is not valid"));
        verify(transactionService, times(0)).saveTransaction(any(Transaction.class));
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), any(Transaction.class));
    }

    @Test
    public void submitTransactionWithExpiredCard() throws Exception {
        SubmitPaymentRequestBody requestBody = new SubmitPaymentRequestBody();
        requestBody.setAmount(100.0);
        requestBody.setMerchantId(1L);
        requestBody.setCcv("333");
        requestBody.setCardNumber(validCardNumber);
        requestBody.setExpiryMonth(5);
        requestBody.setExpiryYear(2020);
        requestBody.setOwner("John Doe");
        requestBody.setCurrency(CurrencyEnum.USD);

        when(transactionService.saveTransaction(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction submittedTransaction = invocation.getArgument(0);
            submittedTransaction.setId(2L); // Simulate saving with an ID
            submittedTransaction.setStatus(TransactionStatus.PENDING); // Simulate saving with an ID
            return submittedTransaction;
        });

        when(merchantService.getMerchantById(1L)).thenReturn(merchant);

        String error = mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions").contentType(MediaType.APPLICATION_JSON) //
                        .content(objectMapper.writeValueAsString(requestBody))) //
                .andExpect(status().isBadRequest()) //
                .andReturn().getResolvedException().getMessage(); //

        assertTrue(StringUtils.contains(error, "Credit card has expired"));
        verify(transactionService, times(0)).saveTransaction(any(Transaction.class));
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), any(Transaction.class));
    }

    @Test
    public void submitTransactionWithUnregistredMerchant() throws Exception {
        SubmitPaymentRequestBody requestBody = new SubmitPaymentRequestBody();
        requestBody.setAmount(100.0);
        requestBody.setMerchantId(1L);
        requestBody.setCcv("333");
        requestBody.setCardNumber(validCardNumber);
        requestBody.setExpiryMonth(5);
        requestBody.setExpiryYear(2300);
        requestBody.setOwner("John Doe");
        requestBody.setCurrency(CurrencyEnum.USD);

        when(transactionService.saveTransaction(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction submittedTransaction = invocation.getArgument(0);
            submittedTransaction.setId(2L); // Simulate saving with an ID
            submittedTransaction.setStatus(TransactionStatus.PENDING); // Simulate saving with an ID
            return submittedTransaction;
        });

        when(merchantService.getMerchantById(1L)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions").contentType(MediaType.APPLICATION_JSON) //
                        .content(objectMapper.writeValueAsString(requestBody))) //
                .andExpect(status().isUnauthorized()); //

        verify(transactionService, times(0)).saveTransaction(any(Transaction.class));
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), any(Transaction.class));
    }

}