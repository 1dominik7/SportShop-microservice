package com.ecommerce.product.product;

import com.ecommerce.product.product.productItem.response.ProductItemResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateResponse {
    private Integer id;
    private String productName;
    private String description;
    private Integer categoryId;
    private List<ProductItemResponse> productItems;
}