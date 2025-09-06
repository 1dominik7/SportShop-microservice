package com.ecommerce.order.shopOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Integer> {
    List<ShopOrder> findByUserId(String userId);

    ShopOrder findByPaymentIntentIdAndUserId(String paymentIntentId,String userId);
}
