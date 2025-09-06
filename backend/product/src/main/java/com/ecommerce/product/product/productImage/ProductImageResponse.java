package com.ecommerce.product.product.productImage;

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
