package com.ecommerce.user.clients.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemOneByColourResponse {
    private Integer productId;
    private Integer productItemId;
    private String productName;
    private String colour;
    private List<ProductItemOneByColour> productItemOneByColour;
    private List<OtherProductItemOneByColour> otherProductItemOneByColours;
    private List<ProductImageResponse> productImages;
}