package com.ecommerce.payment.clients.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStockBatchUpdateRequest {
    private List<ProductStockUpdateRequest> updates;
}
