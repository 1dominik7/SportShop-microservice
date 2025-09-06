package com.ecommerce.marketing.clients.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscountCodeResponse {

    private String id;
    private String name;
    private String code;
    private LocalDateTime expiryDate;
    private Integer discount;
    private boolean singleUse;
    private boolean used;
}
