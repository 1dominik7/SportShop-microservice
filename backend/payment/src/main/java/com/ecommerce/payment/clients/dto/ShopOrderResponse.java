package com.ecommerce.payment.clients.dto;

import com.ecommerce.payment.payment.Payment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderResponse {

    private Integer id;
    private String userId;
    private LocalDateTime orderDate;
    private ShippingMethodResponse shippingMethod;
    private Double orderTotal;
    private Double finalOrderTotal;
    private OrderStatusResponse orderStatus;
    private List<OrderLineResponse> orderLines = new ArrayList<>();
    private Integer paymentId;
    private Integer appliedDiscountValue;
    private String paymentTransactionId;
    private String paymentIntentId;
    private String paymentMethodName;
    private LocalDateTime paymentCreatedAt;
    private LocalDateTime paymentUpdatedAt;
    private Payment.PaymentStatus paymentStatus;
    private String shippingFirstName;
    private String shippingLastName;
    private String shippingStreet;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingCountry;
    private String shippingPhoneNumber;
}
