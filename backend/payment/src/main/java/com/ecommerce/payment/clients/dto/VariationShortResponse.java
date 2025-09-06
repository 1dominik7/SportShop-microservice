package com.ecommerce.payment.clients.dto;

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
