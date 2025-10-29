package com.ecommerce.user.shoppingCart;

import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItemResponse;
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
