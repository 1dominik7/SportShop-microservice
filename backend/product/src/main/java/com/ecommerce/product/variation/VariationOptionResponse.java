package com.ecommerce.product.variation;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationOptionResponse {
    private Integer id;
    private String value;
}
