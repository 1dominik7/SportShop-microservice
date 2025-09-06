package com.ecommerce.user.clients.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductItemRequest {
    private Integer id;
    private Double price;
    private Integer discount;
    private String productCode;
    private Integer qtyInStock;
    private List<VariationResponse> variations;
    private List<ProductImageResponse> productImages;
    private Integer productId;
    private String productName;
    private String colour;
    private String size;
}
