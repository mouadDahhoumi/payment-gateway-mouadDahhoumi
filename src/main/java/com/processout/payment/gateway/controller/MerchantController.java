package com.processout.payment.gateway.controller;

import com.processout.payment.gateway.model.Merchant;
import com.processout.payment.gateway.service.IMerchantService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    private final IMerchantService merchantService;

    public MerchantController(IMerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    public List<Merchant> getAllMerchants() {
        return merchantService.getAllMerchants();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable Long id) {
        Merchant merchant = merchantService.getMerchantById(id);
        if (merchant != null) {
            return ResponseEntity.ok(merchant);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> createMerchant(@RequestBody Merchant merchant) {
        Merchant savedMerchant = merchantService.saveMerchant(merchant);
        return ResponseEntity.created(URI.create("/api/merchants/" + savedMerchant.getId())).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        if (merchantService.getMerchantById(id) != null) {
            merchantService.deleteMerchant(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
