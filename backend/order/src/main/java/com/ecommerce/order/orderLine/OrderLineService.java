package com.ecommerce.order.orderLine;

import com.ecommerce.order.clients.UserCallerService;
import com.ecommerce.order.clients.UserClient;
import com.ecommerce.order.clients.dto.UserResponse;
import com.ecommerce.order.exceptions.NotFoundException;
import com.stripe.model.climate.Order;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final UserCallerService userCallerService;

    public List<OrderLineResponse> getOrderLinesByIds(List<Integer> orderLineIds ){
        List<OrderLine> orderLines = orderLineRepository.findAllById(orderLineIds);

        List<OrderLineResponse> orderLineResponse = orderLines.stream().map(
                orderLine ->
                OrderLineResponse.builder()
                        .id(orderLine.getId())
                        .productName(orderLine.getProductName())
                        .productItemId(orderLine.getProductItemId())
                        .qty(orderLine.getQty())
                        .price(orderLine.getPrice())
                        .build()
        )
                .collect(Collectors.toList());

        return orderLineResponse;
    }

    public OrderLineResponse getOrderLineById(Integer orderLineId, Jwt jwt) throws AccessDeniedException {
        OrderLine orderLine = orderLineRepository.findById(orderLineId).orElseThrow(() -> new NotFoundException("OrderLine" , Optional.of(orderLineId.toString())));

        UserResponse currentUser = userCallerService.getUserProfile(jwt);
        String ownerUserId = orderLine.getShopOrder().getUserId();

        if (!ownerUserId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to view this order line");
        }

        return OrderLineResponse.builder()
                .id(orderLine.getId())
                .productName(orderLine.getProductName())
                .productItemId(orderLine.getProductItemId())
                .qty(orderLine.getQty())
                .price(orderLine.getPrice())
                .build();
    }

    public List<OrderLineResponse> getOrderLineByProductItemIds(List<Integer> productItemIds){
        if (productItemIds == null || productItemIds.isEmpty()) {
            return List.of();
        }
        List<OrderLine> orderLines = orderLineRepository.findByProductItemIdIn(productItemIds);

        if (orderLines.isEmpty()) {
            return List.of();
        }

        return orderLines.stream().map( order ->
                OrderLineResponse.builder()
                        .id(order.getId())
                        .productName(order.getProductName())
                        .productItemId(order.getProductItemId())
                        .qty(order.getQty())
                        .price(order.getPrice())
                        .build()
        ).collect(Collectors.toList());
    }

    public boolean canUserReviewOrderLine(Integer orderLineId, Integer productItemId, Jwt jwt){

    UserResponse user = userCallerService.getUserProfile(jwt);

        return orderLineRepository.existsByIdAndProductItemIdAndShopOrder_UserId(orderLineId, productItemId, user.getId());
}

}
