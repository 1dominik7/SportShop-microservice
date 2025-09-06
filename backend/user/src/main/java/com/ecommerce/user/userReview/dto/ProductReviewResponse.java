package com.ecommerce.user.userReview.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductReviewResponse {
    private Integer productId;
    private List<UserReviewResponse> reviews;
    private Double averageRating;
    private Integer totalReviews;
}
