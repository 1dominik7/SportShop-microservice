package com.ecommerce.product.product.productItem.response;

import com.ecommerce.product.product.productImage.ProductImageResponse;
import com.ecommerce.product.variation.VariationOptionWithVariationResponse;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemResponseToOrderShop {
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