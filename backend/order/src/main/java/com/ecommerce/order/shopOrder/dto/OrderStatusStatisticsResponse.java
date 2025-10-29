package com.ecommerce.order.shopOrder;

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
