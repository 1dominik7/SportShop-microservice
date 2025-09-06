package com.ecommerce.product.product.productImage;

import com.ecommerce.product.product.productItem.ProductItem;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Image filename is required")
    private String imageFilename;

    @ManyToMany(mappedBy = "productImages")
    @JsonBackReference
    private Set<ProductItem> productItems = new HashSet<>();
}
