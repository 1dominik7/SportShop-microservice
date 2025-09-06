package com.ecommerce.payment.clients.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineResponse {
    private Integer id;
    private String productName;
    private ProductItemToOrderResponse productItem;
    private Integer qty;
    private Double price;
}
