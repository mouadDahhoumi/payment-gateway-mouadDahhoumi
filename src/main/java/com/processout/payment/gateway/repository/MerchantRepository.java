package com.processout.payment.gateway.repository;

import com.processout.payment.gateway.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    // You can define custom query methods here if needed
}
