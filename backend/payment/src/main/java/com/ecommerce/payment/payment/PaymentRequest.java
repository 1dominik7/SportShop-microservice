package com.ecommerce.payment.payment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {

    private String transactionId;
    private String paymentIntentId;
    private Integer shopOrderId;
    private Integer paymentTypeId;
    private String provider;
    private String last4CardNumber;
    private LocalDateTime paymentDate;
    private String userPaymentMethodId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Payment.PaymentStatus status;
}
