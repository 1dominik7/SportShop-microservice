package com.ecommerce.user.userReview.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductReviewSummaryResponse{
    private Integer productId;
    private Double averageRating;
    private Integer totalReviews;
}
