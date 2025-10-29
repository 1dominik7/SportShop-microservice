package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.dto.ShopOrderRequest;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayAgainRequest {
    private Integer orderId;
    private String successUrl;
    private String cancelUrl;
}
