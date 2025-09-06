package com.ecommerce.user.shoppingCart;

import com.ecommerce.user.discountCode.DiscountCode;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCarItemGetProdItemResponse;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartGetProdItemResponse {
        private List<ShoppingCarItemGetProdItemResponse> shoppingCartItems;
        private Set<DiscountCode> discountCodes;
}
