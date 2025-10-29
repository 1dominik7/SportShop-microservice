package com.ecommerce.payment.payment.PayUDTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayUBuyer {
    private String customerId;
    private String email;
    private String language;
}
