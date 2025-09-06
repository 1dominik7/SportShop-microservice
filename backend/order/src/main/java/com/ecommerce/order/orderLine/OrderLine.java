package com.ecommerce.order.orderLine;

import com.ecommerce.order.shopOrder.ShopOrder;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String productName;
    private Integer productItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    @JsonBackReference
    private ShopOrder shopOrder;
    private Integer qty;
    private Double price;
    private List<String> userReviewIds;
}
