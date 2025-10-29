package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.dto.PaymentStatus;
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
    private PaymentStatus paymentStatus;
    private String orderStatus;
}
