package com.ecommerce.user.clients.dto;

import com.ecommerce.user.userPaymentMethod.UserPaymentMethod;
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
        private UserPaymentMethod paymentMethod;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private PaymentStatus status;
}
