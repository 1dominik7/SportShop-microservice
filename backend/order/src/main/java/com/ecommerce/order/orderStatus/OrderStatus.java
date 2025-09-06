package com.ecommerce.order.orderStatus;

import com.ecommerce.order.shopOrder.ShopOrder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String status;

    @OneToMany(mappedBy = "orderStatus", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ShopOrder> shopOrders = new ArrayList<>();
}
