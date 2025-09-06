package com.ecommerce.user.userReview;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_reviews")
public class UserReview {

    @Id
    private String id;
    private String userId;
    private Integer orderLineId;
    private Integer ratingValue;
    private String comment;

    @CreatedDate
    private LocalDateTime createdDate;
}
