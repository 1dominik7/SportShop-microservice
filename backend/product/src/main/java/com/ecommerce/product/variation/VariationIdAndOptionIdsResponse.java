package com.ecommerce.product.variation;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationIdAndOptionIdsResponse {
    private Integer variationId;
    private List<Integer> variationOptionIds;
}
