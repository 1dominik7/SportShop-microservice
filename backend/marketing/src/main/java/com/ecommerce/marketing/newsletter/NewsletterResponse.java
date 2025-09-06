package com.ecommerce.marketing.newsletter;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsletterResponse {
    private Integer id;
    private String couponId;
    private String email;
    private String subscribedAt;
}
