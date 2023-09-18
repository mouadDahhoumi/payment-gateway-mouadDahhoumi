package com.processout.payment.gateway.service;

import com.processout.payment.gateway.model.Merchant;

import java.util.List;

public interface IMerchantService {
    // Example methods using the repository
    List<Merchant> getAllMerchants();

    Merchant getMerchantById(Long id);

    Merchant saveMerchant(Merchant merchant);

    void deleteMerchant(Long id);
}
