package com.ecommerce.order.clients.dto;

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
