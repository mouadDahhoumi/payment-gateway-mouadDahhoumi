package com.processout.payment.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processout.payment.gateway.dto.SubmitPaymentRequestBody;
import com.processout.payment.gateway.model.*;
import com.processout.payment.gateway.service.IMerchantService;
import com.processout.payment.gateway.service.ITransactionService;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;
import java.util.List;

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

    @Before
    public void setUp() {
        merchant = new Merchant(1, "Merchant 1");
        CardDetails cardDetails = CardDetails.builder().owner("John Doe").expiryYear(2023).expiryMonth(12).cardNumber(validCardNumber).ccv("333").build();
        Transaction transaction = Transaction.builder().id(1L).amount(100.0).merchant(merchant).status(TransactionStatus.PENDING).submissionDate(new Date()).currency(CurrencyEnum.USD).cardDetails(cardDetails).build();
        transactions = List.of(transaction);
    }

    @Test
    public void testSubmitTransaction() throws Exception {
        SubmitPaymentRequestBody requestBody = new SubmitPaymentRequestBody();
        requestBody.setAmount(100.0);
        requestBody.setMerchantId(1L);
        requestBody.setCcv("333");
        requestBody.setCardNumber(validCardNumber);
        requestBody.setExpiryMonth(12);
        requestBody.setExpiryYear(2023);
        requestBody.setOwner("John Doe");
        requestBody.setCurrency(CurrencyEnum.USD);

        when(transactionService.saveTransaction(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction submittedTransaction = invocation.getArgument(0);
            submittedTransaction.setId(2L); // Simulate saving with an ID
            return submittedTransaction;
        });

        when(merchantService.getMerchantById(1L)).thenReturn(merchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions/submit/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestBody))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").value("PENDING"));

        verify(transactionService, times(1)).saveTransaction(any(Transaction.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), any(Transaction.class));
    }

    @Test
    public void testGetTransactionsByMerchantId() throws Exception {
        when(transactionService.findAllTransactionsByMerchantId(1L)).thenReturn(transactions);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/merchant/1")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$[0].id").value(1)).andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(transactionService, times(1)).findAllTransactionsByMerchantId(any());
    }

    @Test
    public void testGetTransactionByIdAndMerchantId() throws Exception {
        when(transactionService.findTransactionByIdAndMerchantId(anyLong(), anyLong())).thenReturn(transactions.get(0));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/merchant/1/transaction/1")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.status").value("PENDING"));
    }
}