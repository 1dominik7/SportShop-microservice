package com.ecommerce.order.orderLine;

import com.ecommerce.order.shopOrder.dto.TopProductSalesDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Integer> {
    List<OrderLine> findByProductItemIdIn(List<Integer> productItemsIds);

    boolean existsByIdAndProductItemIdAndShopOrder_UserId(Integer orderLineId, Integer productItemId, String userId);

    @Query("SELECT ol FROM OrderLine ol JOIN ol.shopOrder so ORDER BY so.orderDate DESC")
    List<OrderLine> findLatestOrderLines(Pageable pageable);

    @Query("""
                SELECT new com.ecommerce.order.shopOrder.dto.TopProductSalesDto(
                    ol.productItemId,
                    SUM(ol.qty)
                )
                FROM OrderLine ol
                JOIN ol.shopOrder so
                WHERE so.orderDate BETWEEN :start AND :end
                GROUP BY ol.productItemId
                ORDER BY SUM(ol.qty) DESC
            """)
    List<TopProductSalesDto> findTopProductItemsBetween(
            LocalDateTime start, LocalDateTime end, Pageable pageable);
}
