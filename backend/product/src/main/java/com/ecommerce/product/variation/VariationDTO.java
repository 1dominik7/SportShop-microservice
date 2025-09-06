package com.ecommerce.product.variation;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationDTO {
    private String variationName;
    private List<VariationOptionResponse> variationOptions;
}
