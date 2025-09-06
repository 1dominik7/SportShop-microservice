package com.ecommerce.user.userReview.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserReviewRequest {

    private Integer productId;
    private Integer orderLineId;
    private Integer ratingValue;
    private String comment;
    private LocalDateTime createdDate;
}
