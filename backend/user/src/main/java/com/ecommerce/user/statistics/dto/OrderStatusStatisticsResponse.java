package com.ecommerce.user.statistics;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusStatisticsResponse {
    private String statusName;
    private Long count;
}
