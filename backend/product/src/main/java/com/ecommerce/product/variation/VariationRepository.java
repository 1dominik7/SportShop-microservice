package com.ecommerce.product.variation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariationRepository extends JpaRepository<Variation, Integer> {

    List<Variation> findByCategoryId(Integer categoryId);
}
