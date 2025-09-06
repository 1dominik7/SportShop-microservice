package com.ecommerce.product.product.productItem.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PItemGroupByColorResponse {
    private Integer id;
    private String colour;
    private String size;
    private Integer productId;
    private Double price;
    private Integer discount;
    private String productCode;
    private Integer qtyInStock;
}
