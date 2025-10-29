package com.ecommerce.user.shoppingCart.shoppingCartItem;

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
