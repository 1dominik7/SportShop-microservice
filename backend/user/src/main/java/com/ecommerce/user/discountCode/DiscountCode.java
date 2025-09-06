package com.ecommerce.user.discountCode;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "discount_code")
public class DiscountCode {

    @Id
    private String id;

    private String name;
    private String code;
    private LocalDateTime expiryDate;
    private Integer discount;

    @Builder.Default
    private boolean used = false;

    @Builder.Default
    private boolean singleUse = false;
}
