package com.ecommerce.user.shoppingCart.shoppingCartItem;

import com.ecommerce.user.clients.dto.ProductItemOneByColourResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCarItemGetProdItemResponse {
    private ProductItemOneByColourResponse productItem;
    private Integer qty;
}
