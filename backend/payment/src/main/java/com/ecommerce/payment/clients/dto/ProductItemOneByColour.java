package com.ecommerce.payment.clients.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemOneByColour {
    private Integer id;
    private Double price;
    private Integer discount;
    private String productCode;
    private Integer qtyInStock;
    private List<VariationResponse> variations;
    private List<ProductImageResponse> productImages;
    private String productName;
    private String productDescription;
    private Integer productId;
    private Integer categoryId;
    private String colour;
}
