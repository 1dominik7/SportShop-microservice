package com.ecommerce.user.userPaymentMethod;

import com.ecommerce.user.clients.dto.PaymentTypeResponse;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPaymentMethod {

    @Id
    private String id;
    private String provider;
    private String last4CardNumber;
    private LocalDateTime paymentDate;
    private LocalDateTime expiryDate;
    private boolean isDefault;
    private PaymentTypeResponse paymentType;
    private boolean active;
}
