package com.ecommerce.product.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 3, message = "Category name must contain at least 3 characters")
    private String categoryName;
    private Integer parentCategoryId;
}


