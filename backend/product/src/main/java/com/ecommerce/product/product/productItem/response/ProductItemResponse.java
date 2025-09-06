package com.ecommerce.product.product.productItem.response;

import com.ecommerce.product.product.productImage.ProductImageResponse;
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
    private List<Integer> variationOptionIds;
    private Set<ProductImageResponse> productImages;
}
