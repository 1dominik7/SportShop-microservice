package com.ecommerce.user.clients.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineResponse {
    private Integer id;
    private String productName;
    private Integer productItemId;
    private Integer qty;
    private Double price;
}
