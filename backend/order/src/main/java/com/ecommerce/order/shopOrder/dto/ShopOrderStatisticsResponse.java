package com.ecommerce.order.shopOrder.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderStatisticsResponse {
    private Long totalOrders;
    private Double totalIncomes;
}
