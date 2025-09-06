package com.ecommerce.payment.paymentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTypeRepository extends JpaRepository<PaymentType, Integer> {
    Optional<PaymentType> findByValue(String value);
}
