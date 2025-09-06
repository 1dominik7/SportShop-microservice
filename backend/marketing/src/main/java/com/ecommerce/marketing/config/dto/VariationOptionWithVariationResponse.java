package com.ecommerce.marketing.config.dto;

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
