package com.ecommerce.product.product.productItem.request;

import com.ecommerce.product.product.productImage.ProductImage;
import com.ecommerce.product.variation.VariationResponse;
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
    private List<ProductImage> productImages;
    private Integer productId;
    private String productName;
    private String colour;
    private String size;
}
