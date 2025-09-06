package com.ecommerce.product.variation;

import com.ecommerce.product.category.Category;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "variation")
public class Variation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    @JsonBackReference
    private Category category;

    @OneToMany(mappedBy = "variation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<VariationOption> variationOptions = new ArrayList<>();
}
