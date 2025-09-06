package com.ecommerce.order.clients.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemResponse {
    private Integer id;
    private Double price;
    private Integer discount;
    private String productCode;
    private Integer qtyInStock;
    private Integer productId;
    private List<VariationOptionWithVariationResponse> variationOptions;
    private Set<ProductImageResponse> productImages;
    private String productName;
    private String productDescription;
}
