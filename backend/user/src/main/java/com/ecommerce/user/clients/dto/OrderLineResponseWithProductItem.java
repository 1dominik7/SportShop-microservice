package com.ecommerce.user.clients.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineResponseWithProductItem {
    private Integer id;
    private String productName;
    private ProductItemToOrderResponse productItem;
    private Integer qty;
    private Double price;
}
