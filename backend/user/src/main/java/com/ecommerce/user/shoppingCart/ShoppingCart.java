package com.ecommerce.user.shoppingCart;

import com.ecommerce.user.discountCode.DiscountCode;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItem;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCart {

    private List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();

    @DBRef
    private Set<DiscountCode> discountCodes = new HashSet<>();
}
