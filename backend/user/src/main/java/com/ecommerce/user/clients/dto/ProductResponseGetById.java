package com.ecommerce.user.clients.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseGetById {
    private Integer id;
    private String productName;
    private String description;
    private Integer categoryId;
    private List<ProductItemResponse> productItems;
}
