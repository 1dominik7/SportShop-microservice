package com.ecommerce.user.clients.dto;


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
