package com.ecommerce.payment.payment;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    Optional<Payment> findByShopOrderId(Integer shopOrderId);

    List<Payment> findAllByShopOrderId(Integer shopOrderId);
}
