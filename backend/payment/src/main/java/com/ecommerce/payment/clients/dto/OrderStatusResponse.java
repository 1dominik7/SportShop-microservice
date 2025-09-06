package com.ecommerce.payment.clients.dto;

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
