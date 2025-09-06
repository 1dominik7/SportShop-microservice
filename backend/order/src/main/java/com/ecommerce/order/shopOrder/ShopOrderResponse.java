package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.dto.PaymentStatus;
import com.ecommerce.order.orderLine.OrderLineResponseWithProductItem;
import com.ecommerce.order.orderStatus.OrderStatus;
import com.ecommerce.order.shippingMethod.ShippingMethodResponse;
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
    private OrderStatus orderStatus;
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
    private String shippingAddressLine1;
    private String shippingAddressLine2;
}
