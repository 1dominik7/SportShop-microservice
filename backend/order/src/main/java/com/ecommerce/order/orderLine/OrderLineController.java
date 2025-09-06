package com.ecommerce.order.orderLine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("order-line")
public class OrderLineController {

    private final OrderLineService orderLineService;

    @GetMapping("/get-by-ids")
    public ResponseEntity<List<OrderLineResponse>> getOrderLinesByIds(@RequestParam List<Integer> orderLinesIds){
        List<OrderLineResponse> orderLineResponses = orderLineService.getOrderLinesByIds(orderLinesIds);
        return ResponseEntity.ok(orderLineResponses);
    }

    @GetMapping("/{orderLineId}")
    public ResponseEntity<OrderLineResponse> getOrderLineById(@PathVariable Integer orderLineId, @AuthenticationPrincipal Jwt jwt) throws AccessDeniedException {
        OrderLineResponse orderLineResponse = orderLineService.getOrderLineById(orderLineId, jwt);
        return ResponseEntity.ok(orderLineResponse);
    }

    @GetMapping("/by-product-item-ids")
    public ResponseEntity<List<OrderLineResponse>> getOrderLineByProductItemIds(@RequestParam List<Integer> productItemIds){
        List<OrderLineResponse> orderLineResponses = orderLineService.getOrderLineByProductItemIds(productItemIds);
        return ResponseEntity.ok(orderLineResponses);
    }

    @GetMapping("/can-review/{orderLineId}")
    public boolean canUserReviewOrderLine(
            @PathVariable Integer orderLineId,
            @RequestParam Integer productItemId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return orderLineService.canUserReviewOrderLine(
                orderLineId, productItemId, jwt
        );
    }
}
