package com.ecommerce.order.orderLine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Integer> {
    List<OrderLine> findByProductItemIdIn(List<Integer> productItemsIds);

    boolean existsByIdAndProductItemIdAndShopOrder_UserId(Integer orderLineId, Integer productItemId, String userId);
}
