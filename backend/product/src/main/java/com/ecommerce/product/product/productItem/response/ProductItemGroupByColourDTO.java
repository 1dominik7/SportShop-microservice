package com.ecommerce.product.product.productItem.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemGroupByColourDTO {
        private List<ProductItemGroupByColorResponse> content;
        private Integer pageNumber;
        private Integer pageSize;
        private long totalElements;
        private Integer totalPages;
        private boolean lastPage;
}
