package com.ecommerce.order.clients.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPaymentResponse {
    private Integer orderId;
    private Integer paymentMethodId;
    private String clientSecret;
    private String paymentUrl;
    private String orderStatus;
    private String paymentIntentId;
    private String currency;
    private Double amount;
}
