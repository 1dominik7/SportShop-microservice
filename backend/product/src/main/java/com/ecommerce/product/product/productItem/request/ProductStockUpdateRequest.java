package com.ecommerce.product.product.productItem.request;

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
