package com.ecommerce.order.orderStatus;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusResponse {
    private Integer id;
    private String status;
}
