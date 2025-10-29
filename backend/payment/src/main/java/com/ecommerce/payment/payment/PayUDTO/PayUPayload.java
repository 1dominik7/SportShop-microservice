package com.ecommerce.payment.payment.PayUDTO;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayUPayload {
    private PayUOrder order;
    private OffsetDateTime localReceiptDateTime;
    private List<PayUProperty> properties;

}
