package com.ecommerce.user.clients.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesRatioStatistics {
    private List<Double> thisMonthWeeklySales;
    private List<Double> lastMonthWeeklySales;
}
