package com.ecommerce.user.userReview.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserReviewResponse {
    private String id;
    private String userName;
    private Integer ratingValue;
    private String comment;
    private LocalDateTime createdDate;
    private Integer orderLineId;
}
