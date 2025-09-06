package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.dto.OrderLineResponse;
import com.ecommerce.payment.clients.dto.ShippingMethodResponse;
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
