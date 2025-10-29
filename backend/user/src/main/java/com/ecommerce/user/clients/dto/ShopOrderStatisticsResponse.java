package com.ecommerce.order.shopOrder;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderStatisticsResponse {
    private Long totalOrders;
    private BigDecimal totalIncomes;
}
