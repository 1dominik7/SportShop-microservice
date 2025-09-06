package com.ecommerce.user.shoppingCart.shoppingCartItem;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartItem {

    private Integer productItemId;
    private Integer qty;
}
