package com.ecommerce.product.product;

import com.ecommerce.product.category.Category;
import com.ecommerce.product.product.productItem.ProductItem;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(min = 3, message = "Product name must contain at least 3 characters")
    private String productName;

    @NotBlank
    @Size(min = 6, message = "Description name must contain at least 6 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ProductItem> productItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    private Category category;
}

