package com.ecommerce.product.product;

import com.ecommerce.product.product.productItem.response.ProductItemResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
    private Optional<Integer> id;
    @NotBlank(message = "Product name is required")
    @Size(min = 3, message = "Product name must contain at least 3 characters")
    private String productName;

    @NotBlank(message = "Description is required")
    @Size(min = 6, message = "Description must contain at least 6 characters")
    private String description;

    @NotEmpty(message = "At least one product item is required")
    private List<ProductItemResponse> productItems;
    private Integer categoryId;
    private LocalDateTime createdDate;
}
