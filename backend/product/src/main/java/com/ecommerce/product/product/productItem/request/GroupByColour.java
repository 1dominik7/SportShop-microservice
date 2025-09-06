package com.ecommerce.product.product.productItem.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupByColour {
    private List<ProductItemRequest> productItemRequests;
    private String value;
}
