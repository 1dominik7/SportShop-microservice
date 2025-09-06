package com.ecommerce.payment.clients.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStockUpdateRequest {
    private Integer productItemId;
    private Integer quantityToSubtract;
}
