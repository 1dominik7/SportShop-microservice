package com.ecommerce.order.orderStatus;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusRequest {
    private String status;
}
