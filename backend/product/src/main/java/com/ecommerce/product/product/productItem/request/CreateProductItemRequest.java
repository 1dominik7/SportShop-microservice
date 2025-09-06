package com.ecommerce.product.product.productItem.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductItemRequest {
    private Double price;
    private Integer discount;
    private String productCode;
    private Integer qtyInStock;
    private Integer productId;
    private List<Integer> variationOptionIds;
    private List<String> productImages;
}
