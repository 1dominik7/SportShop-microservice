package com.ecommerce.order.shippingMethod;

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
@Table(name = "shipping_method")
public class ShippingMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Double price;

    @OneToMany(mappedBy = "shippingMethod", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ShopOrder> shopOrders = new ArrayList<>();
}
