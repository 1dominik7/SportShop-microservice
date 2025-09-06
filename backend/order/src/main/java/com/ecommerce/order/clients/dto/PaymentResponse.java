package com.ecommerce.order.clients.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {

        private Integer id;
        private String transactionId;
        private String paymentIntentId;
        private Integer shopOrderId;
        private UserPaymentMethodResponse paymentMethod;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
         private PaymentStatus status;
}
