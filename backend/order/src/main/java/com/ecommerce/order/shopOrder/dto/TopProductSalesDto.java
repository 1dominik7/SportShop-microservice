package com.ecommerce.order.shopOrder.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopProductSalesDto {
    private Integer productItemId;
    private Long totalQuantity;
}
