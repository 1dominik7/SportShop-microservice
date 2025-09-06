package com.ecommerce.marketing.config.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {
    private Long id;
    private String imageFilename;
}
