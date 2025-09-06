package com.ecommerce.user.clients.dto;

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
