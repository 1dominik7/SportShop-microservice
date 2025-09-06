package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.dto.ShopOrderResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentVerificationResponse {
    private Integer id;
    private String transactionId;
    private String paymentIntentId;
    private ShopOrderResponse shopOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Payment.PaymentStatus status;

}
