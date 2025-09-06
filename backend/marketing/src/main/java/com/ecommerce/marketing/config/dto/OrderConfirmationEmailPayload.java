package com.ecommerce.marketing.config.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderConfirmationEmailPayload {
    private Integer orderId;
    private String email;
    private List<OrderLineResponse> orderLines;
    private LocalDateTime orderDate;
    private Double totalPrice;
    private ShippingMethodResponse shippingMethodResponse;
}
