package com.ecommerce.order.orderLine;

import com.ecommerce.order.clients.dto.ProductItemToOrderResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineResponseWithProductItem {
    private Integer id;
    private String productName;
    private ProductItemToOrderResponse productItem;
    private Integer qty;
    private Double price;
}
