package com.ecommerce.product.category;

import com.ecommerce.product.variation.VariationResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithVariationResponse {
    private Integer id;
    private String categoryName;
    private Integer parentCategoryId;
    private List<VariationResponse> variations;
}
