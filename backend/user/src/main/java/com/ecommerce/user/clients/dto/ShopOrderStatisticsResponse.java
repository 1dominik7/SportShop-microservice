package com.ecommerce.user.clients.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderStatisticsResponse {
    private Long totalOrders;
    private Double totalIncomes;
}
