package com.processout.payment.gateway.service;

import com.processout.payment.gateway.model.Merchant;
import com.processout.payment.gateway.repository.MerchantRepository;
import com.processout.payment.gateway.service.impl.MerchantService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantService merchantService;

    @Test
    public void testGetAllMerchants() {
        List<Merchant> merchants = Arrays.asList(
                new Merchant(1L, "Merchant 1"),
                new Merchant(2L, "Merchant 2")
        );

        when(merchantRepository.findAll()).thenReturn(merchants);

        List<Merchant> result = merchantService.getAllMerchants();

        assertEquals(2, result.size());
        assertEquals("Merchant 1", result.get(0).getName());
    }

}
