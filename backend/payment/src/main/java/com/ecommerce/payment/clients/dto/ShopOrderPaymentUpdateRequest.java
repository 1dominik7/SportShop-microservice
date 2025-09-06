package com.ecommerce.payment.clients.dto;

import com.ecommerce.payment.payment.Payment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderPaymentUpdateRequest {
    private Integer paymentId;
    private String paymentTransactionId;
    private String paymentIntentId;
    private String paymentMethodName;
    private LocalDateTime paymentCreatedAt;
    private Payment.PaymentStatus paymentStatus;
    private String orderStatus;
}
