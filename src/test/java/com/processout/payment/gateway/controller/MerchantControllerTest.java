package com.processout.payment.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processout.payment.gateway.model.Merchant;
import com.processout.payment.gateway.service.IMerchantService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(MerchantController.class)
public class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IMerchantService merchantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllMerchants() throws Exception {
        // Create a list of sample merchants for testing
        List<Merchant> merchants = Arrays.asList(
                new Merchant(1L, "Merchant 1"),
                new Merchant(2L, "Merchant s2")
        );

        // Mock the service to return the sample data
        when(merchantService.getAllMerchants()).thenReturn(merchants);

        // Perform a GET request to the /api/merchants endpoint
        mockMvc.perform(get("/api/merchants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetMerchantById() throws Exception {
        Long merchantId = 1L;
        Merchant merchant = new Merchant(merchantId, "Merchant 1");
        when(merchantService.getMerchantById(merchantId)).thenReturn(merchant);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/merchants/{id}", merchantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Merchant 1"));

        verify(merchantService, times(1)).getMerchantById(merchantId);
    }

    @Test
    public void testCreateMerchant() throws Exception {
        Merchant newMerchant = new Merchant(3L, "New Merchant");
        when(merchantService.saveMerchant(any(Merchant.class))).thenReturn(newMerchant);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMerchant)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/merchants/3"));

        verify(merchantService, times(1)).saveMerchant(any(Merchant.class));
    }

    @Test
    public void testDeleteMerchant() throws Exception {
        Long merchantId = 1L;
        when(merchantService.getMerchantById(merchantId)).thenReturn(new Merchant(merchantId, "Merchant 1"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/merchants/{id}", merchantId))
                .andExpect(status().isNoContent());

        verify(merchantService, times(1)).deleteMerchant(merchantId);
    }

    @Test
    public void testDeleteMerchant_not_found() throws Exception {
        Long merchantId = 1L;
        when(merchantService.getMerchantById(merchantId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/merchants/{id}", merchantId))
                .andExpect(status().isNotFound());

        verify(merchantService, times(0)).deleteMerchant(merchantId);
    }
}
