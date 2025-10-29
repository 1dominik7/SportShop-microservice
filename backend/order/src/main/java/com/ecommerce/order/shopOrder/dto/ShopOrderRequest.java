package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.dto.AddressRequest;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderRequest {

    private String userId;
    private LocalDateTime orderDate;
    private AddressRequest addressRequest;
    private Integer shippingMethodId;
    private Double orderTotal;
    private Double finalOrderTotal;
    private Integer appliedDiscountValue;
}
