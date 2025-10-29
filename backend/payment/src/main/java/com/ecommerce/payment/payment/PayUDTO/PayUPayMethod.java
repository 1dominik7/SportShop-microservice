package com.ecommerce.payment.payment.PayUDTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayUPayMethod {
    private String amount;
    private String type;
}
