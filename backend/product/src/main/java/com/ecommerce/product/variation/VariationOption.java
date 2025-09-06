package com.ecommerce.product.variation;

import com.ecommerce.product.product.productItem.ProductItem;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "variationOption")
public class VariationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String value;

    @ManyToOne
    @JoinColumn(name = "variation_id",nullable = true)
    @JsonBackReference
    private Variation variation;

    @ManyToMany(mappedBy = "variationOptions", cascade= {CascadeType.ALL, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private List<ProductItem> productItems;
}
