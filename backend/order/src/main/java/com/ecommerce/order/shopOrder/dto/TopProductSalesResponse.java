package com.ecommerce.order.shopOrder.dto;


import com.ecommerce.order.clients.dto.ProductItemToOrderResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopProductSalesResponse {
    private ProductItemToOrderResponse productItem;
    private Long totalQuantity;
}
