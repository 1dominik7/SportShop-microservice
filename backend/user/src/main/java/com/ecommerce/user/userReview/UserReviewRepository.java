package com.ecommerce.user.userReview;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserReviewRepository extends MongoRepository<UserReview, String> {

    boolean existsByUserIdAndOrderLineId(String userId, Integer orderLineId);

    List<UserReview> findByOrderLineIdIn (List<Integer> orderLineIds);

}
