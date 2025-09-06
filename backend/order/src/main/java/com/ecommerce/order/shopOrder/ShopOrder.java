package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.dto.PaymentStatus;
import com.ecommerce.order.orderLine.OrderLine;
import com.ecommerce.order.orderStatus.OrderStatus;
import com.ecommerce.order.shippingMethod.ShippingMethod;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shop_order")
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String userId;
    private LocalDateTime orderDate;

    @ManyToOne
    @JoinColumn(name = "shipping_method_id", nullable = false)
    private ShippingMethod shippingMethod;
    private Double orderTotal;
    private Double finalOrderTotal;

    @ManyToOne
    @JoinColumn(name = "order_status_id")
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "shopOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderLine> orderLines = new ArrayList<>();
    private Integer paymentId;
    private Integer appliedDiscountValue;
    private String paymentTransactionId;
    private String paymentIntentId;
    private String paymentMethodName;
    private LocalDateTime paymentCreatedAt;

    @Enumerated(EnumType.STRING)
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
