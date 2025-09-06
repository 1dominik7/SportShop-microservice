package com.ecommerce.product.category;

import com.ecommerce.product.product.Product;
import com.ecommerce.product.variation.Variation;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 3, message = "Category name must contain at least 3 characters")
    private String categoryName;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Product> products = new ArrayList<>();

     @OneToMany(mappedBy = "category", cascade = {CascadeType.ALL, CascadeType.REMOVE}, fetch = FetchType.LAZY)
     @JsonManagedReference
     @Builder.Default
    private List<Variation> variations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "parent_category_id")
    @JsonBackReference
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Category> subcategories= new ArrayList<>();;
}
