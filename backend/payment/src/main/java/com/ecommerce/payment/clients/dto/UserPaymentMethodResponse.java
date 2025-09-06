package com.ecommerce.payment.clients.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPaymentMethodResponse {

    private String id;
    private Integer UserId;
    private String paymentTypeName;
    private String provider;
    private String last4CardNumber;
    private LocalDateTime paymentDate;
    private LocalDateTime expiryDate;
    private boolean isDefault;
}
