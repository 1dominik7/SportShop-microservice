package com.ecommerce.payment.clients.dto;

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
    private List<ProductImageResponse> productImages;
    private String productName;
    private String productDescription;
}
