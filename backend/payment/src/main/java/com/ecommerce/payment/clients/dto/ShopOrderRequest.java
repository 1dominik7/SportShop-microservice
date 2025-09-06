package com.ecommerce.payment.clients.dto;

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
