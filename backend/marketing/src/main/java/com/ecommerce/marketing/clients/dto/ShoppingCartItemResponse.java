package com.ecommerce.marketing.clients.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartItemResponse {
    private Integer productItemId;
    private Integer qty;
}
