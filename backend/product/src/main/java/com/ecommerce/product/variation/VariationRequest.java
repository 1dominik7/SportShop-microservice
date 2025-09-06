package com.ecommerce.product.variation;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationRequest {

    private Integer categoryId;
    private String name;
}
