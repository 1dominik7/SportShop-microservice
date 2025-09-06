package com.ecommerce.product.variation;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationOptionRequest {
    private Integer variationId;
    private String value;
}
