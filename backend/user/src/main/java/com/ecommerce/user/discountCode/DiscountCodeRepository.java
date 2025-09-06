package com.ecommerce.user.discountCode;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends MongoRepository<DiscountCode, String> {

    List<DiscountCode> findByExpiryDateGreaterThanEqual(LocalDateTime now);

    Optional<DiscountCode> findByCode(String code);
}
