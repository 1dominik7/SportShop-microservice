package com.ecommerce.product.product;

import com.ecommerce.product.product.productItem.ProductItem;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer id;
    private String productName;
    private String description;
    private Integer categoryId;
    private List<ProductItem> productItemList;
}
