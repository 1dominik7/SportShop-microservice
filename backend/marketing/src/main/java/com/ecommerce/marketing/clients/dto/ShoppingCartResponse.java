package com.ecommerce.marketing.clients.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartResponse {
    private List<ShoppingCartItemResponse> shoppingCartItems;
}
