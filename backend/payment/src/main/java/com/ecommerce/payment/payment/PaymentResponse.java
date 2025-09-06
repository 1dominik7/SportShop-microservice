package com.ecommerce.payment.payment;

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
        private String userPaymentId;
        private String provider;
        private String last4CardNumber;
        private LocalDateTime paymentDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Payment.PaymentStatus status;
}
