package com.ecommerce.order.clients.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartItemResponse {
    private Integer productItemId;
    private Integer qty;
    private String productName;
}
