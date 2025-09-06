package com.ecommerce.order.clients.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherProductItemOneByColour {
    private Integer productId;
    private String productName;
    private String colour;
    private Set<ProductImageResponse> productImages;
    private List<ProductItemResponse> otherColourVariation;
}
