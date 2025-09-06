package com.ecommerce.marketing.config.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationShortResponse {
    private Integer id;
    private String name;
}
