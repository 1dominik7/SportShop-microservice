package com.ecommerce.product.product.productItem.response;

import com.ecommerce.product.product.productImage.ProductImage;
import com.ecommerce.product.product.productItem.request.ProductItemRequest;
import com.ecommerce.product.variation.VariationResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemGroupByColorResponse {
    private Integer productId;
    private String productName;
    private String colour;
    private List<ProductImage> productImages;
    private List<VariationResponse> variations;
    private List<ProductItemRequest> productItemRequests;
}
