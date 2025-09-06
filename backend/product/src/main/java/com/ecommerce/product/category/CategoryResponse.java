package com.ecommerce.product.category;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Integer id;
    private String categoryName;
    private Integer parentCategoryId;
    private List<Integer> variationIds;
}
