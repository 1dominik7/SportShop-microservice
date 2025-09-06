package com.ecommerce.product.product.productItem.response;

import com.ecommerce.product.product.productImage.ProductImage;
import com.ecommerce.product.variation.VariationOptionWithVariationResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemToOrderResponse {
    private Integer id;
    private Double price;
    private Integer discount;
    private String productCode;
    private Integer qtyInStock;
    private Integer productId;
    private List<VariationOptionWithVariationResponse> variationOptions;
    private List<ProductImage> productImages;
    private String productName;
    private String productDescription;
}
