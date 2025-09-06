package com.ecommerce.product.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.productItems pi " +
            "JOIN pi.variationOptions vo " +
            "WHERE p.category.id = :categoryId " +
            "AND ((:variationIds IS NULL OR vo.variation.id IN :variationIds) " +
            "AND (:variationOptionIds IS NULL OR vo.id IN :variationOptionIds))")
    Page<Product> findByVariationsAndValues(
            @Param("categoryId") Integer categoryId,
            @Param("variationIds") List<Integer> variationIds,
            @Param("variationOptionIds") List<Integer> variationOptionIds,
            Pageable pageable);


    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

}

