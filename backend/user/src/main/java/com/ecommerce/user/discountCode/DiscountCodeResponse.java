package com.ecommerce.user.discountCode;

import com.ecommerce.user.shoppingCart.ShoppingCart;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscountCodeResponse {

    private String id;
    private String name;
    private String code;
    private LocalDateTime expiryDate;
    private Integer discount;
    private boolean singleUse;
    private boolean used;
}
