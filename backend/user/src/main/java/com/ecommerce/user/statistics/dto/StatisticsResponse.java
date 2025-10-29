package com.ecommerce.user.statistics.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsResponse {
    private UsersStatistics usersStatistics;
    private Long totalProducts;
    private Long totalOrders;
    private Double totalIncomes;
}
