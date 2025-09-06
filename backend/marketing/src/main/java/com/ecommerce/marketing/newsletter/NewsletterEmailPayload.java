package com.ecommerce.marketing.newsletter;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsletterEmailPayload {
    private Integer id;
    private String email;
    private String discountCode;
}
