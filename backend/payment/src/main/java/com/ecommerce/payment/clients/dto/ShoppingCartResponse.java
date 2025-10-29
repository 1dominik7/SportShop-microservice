package com.ecommerce.payment.clients.dto;

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
