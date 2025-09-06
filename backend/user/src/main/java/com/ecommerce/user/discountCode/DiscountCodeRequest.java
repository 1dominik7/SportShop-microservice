package com.ecommerce.user.discountCode;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscountCodeRequest {
    private String name;
    private String code;
    private LocalDateTime expiryDate;
    private Integer discount;
    private boolean singleUse;
    private boolean used;
}
