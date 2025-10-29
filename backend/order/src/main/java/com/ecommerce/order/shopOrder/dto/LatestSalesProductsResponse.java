package com.ecommerce.order.shopOrder.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LatestSalesProductsResponse {
    private Integer productId;
    private Integer productItemId;
    private String productName;
    private Integer orderId;
    private LocalDateTime orderCreatedDate;
    private Double price;
    private Integer quantity;
    private String orderStatus;
}
