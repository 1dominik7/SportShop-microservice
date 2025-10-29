package com.ecommerce.order.shopOrder;

import com.ecommerce.order.shopOrder.dto.OrderStatusStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Integer> {
    List<ShopOrder> findByUserId(String userId);

    ShopOrder findByPaymentIntentIdAndUserId(String paymentIntentId, String userId);

    Page<ShopOrder> findAll(Pageable pageable);

    @Query("SELECT order FROM ShopOrder order where order.id = :id")
    Page<ShopOrder> findById(@Param("id") Integer id, Pageable pageable);

    Page<ShopOrder> findByUserIdContainingIgnoreCase(String userId, Pageable pageable);

    @Query("SELECT SUM(o.finalOrderTotal) FROM ShopOrder o")
    Double getTotalOrderSum();

    @Query("""
            SELECT os.status AS statusName, COUNT(so) AS count
            FROM ShopOrder so
            JOIN so.orderStatus os
            GROUP BY os.status
            ORDER BY count DESC
            """
    )
    List<OrderStatusStatisticsResponse> findTopOrderStatuses(Pageable pageable);

    List<ShopOrder> findAllByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
