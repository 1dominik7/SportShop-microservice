package com.ecommerce.product.product.productItem.response;

import com.ecommerce.product.variation.VariationResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemFiltersResponse {
    private Integer categoryId;
    private VariationResponse variation;
}
