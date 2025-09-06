package com.ecommerce.product.product.productItem.response;

import com.ecommerce.product.product.productImage.ProductImage;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherProductItemOneByColour {
    private Integer productId;
    private Integer productItemId;
    private String productName;
    private String colour;
    private Set<ProductImage> productImages;
    private List<ProductItemResponse> otherColourVariation;
}
