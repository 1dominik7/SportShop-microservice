package com.ecommerce.user.clients.dto;


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
    private List<OrderLineResponseWithProductItem> orderLines = new ArrayList<>();
    private Integer paymentId;
    private Integer appliedDiscountValue;
    private String paymentTransactionId;
    private String paymentIntentId;
    private String paymentMethodName;
    private LocalDateTime paymentCreatedAt;
    private PaymentStatus paymentStatus;
    private String shippingFirstName;
    private String shippingLastName;
    private String shippingStreet;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingCountry;
    private String shippingPhoneNumber;
}
