package com.ecommerce.product.variation;

import com.ecommerce.product.variation.VariationShortResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationOptionWithVariationResponse {
    private Integer id;
    private String value;
    private VariationShortResponse variation;
}
