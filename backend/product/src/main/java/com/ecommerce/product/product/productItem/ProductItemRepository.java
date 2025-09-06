package com.ecommerce.product.product.productItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Integer> {

    @Query("SELECT DISTINCT pi FROM ProductItem pi " +
            "JOIN pi.variationOptions vo " +
            "JOIN vo.variation v " +
            "JOIN pi.product p " +
            "WHERE (:variationOptionIds IS NULL OR vo.id IN :variationOptionIds) " +
            "AND (:variationIds IS NULL OR v.id IN :variationIds) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    List<ProductItem> findByFilters(
            @Param("categoryId") Integer categoryId,
            @Param("variationIds") List<Integer> variationIds,
            @Param("variationOptionIds") List<Integer> variationOptionIds);

    @Query("SELECT pi FROM ProductItem pi " +
            "JOIN FETCH pi.product p " +
            "WHERE p.category.id = :categoryId")
    List<ProductItem> findByCategoryId( @Param("categoryId") Integer categoryId);


    List<ProductItem> findByProductId(Integer productId);
}
