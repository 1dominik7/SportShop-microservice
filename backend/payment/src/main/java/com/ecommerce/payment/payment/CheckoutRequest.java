package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.dto.ShopOrderRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripeCheckoutRequest {
    private ShopOrderRequest orderRequest;
    private String successUrl;
    private String cancelUrl;
}
