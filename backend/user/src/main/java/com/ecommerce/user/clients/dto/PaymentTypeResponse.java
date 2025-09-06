package com.ecommerce.user.clients.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentTypeResponse {
    public Integer id;
    public String value;
}
