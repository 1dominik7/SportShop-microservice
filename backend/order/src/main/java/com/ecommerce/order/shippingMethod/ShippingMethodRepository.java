package com.ecommerce.order.shippingMethod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Integer> {
    Optional<ShippingMethod> findByName(String name);
}
