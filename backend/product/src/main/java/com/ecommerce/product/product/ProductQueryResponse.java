package com.ecommerce.product.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductQueryResponse {

    private Integer id;
    private Integer productItemId;
    private String imageUrl;
    private String productName;
    private Double price;
    private Integer discount;
    private String colour;
}
