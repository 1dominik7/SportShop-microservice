package com.ecommerce.user.statistics;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsResponse {
    private Long totalUsers;
    private Long totalProducts;
    private Long totalOrders;
    private Double totalIncomes;
}
