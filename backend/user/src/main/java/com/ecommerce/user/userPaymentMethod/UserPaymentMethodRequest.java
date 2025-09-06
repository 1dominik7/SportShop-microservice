package com.ecommerce.user.userPaymentMethod;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPaymentMethodRequest {

    private Integer paymentTypeId;
    private String provider;
    private String last4CardNumber;
    private LocalDateTime paymentDate;
    private LocalDateTime expiryDate;
    private boolean isDefault;
}
