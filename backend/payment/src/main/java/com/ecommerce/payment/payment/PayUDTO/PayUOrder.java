package com.ecommerce.payment.payment.PayUDTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayUOrder {
    private String orderId;
    private String extOrderId;
    private String orderCreateDate;
    private String notifyUrl;
    private String customerIp;
    private String merchantPosId;
    private String description;
    private String currencyCode;
    private String totalAmount;
    private PayUBuyer buyer;
    private PayUPayMethod payMethod;
    private String status;
}
