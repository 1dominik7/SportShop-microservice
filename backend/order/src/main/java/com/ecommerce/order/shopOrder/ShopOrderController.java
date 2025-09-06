package com.ecommerce.order.shopOrder;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("shop-order")
public class ShopOrderController {

    private final ShopOrderService shopOrderService;

    @PostMapping("/create")
    public ResponseEntity<ShopOrderResponse> createOrder(@RequestBody ShopOrderRequest request, @AuthenticationPrincipal Jwt jwt) {
        ShopOrderResponse order = shopOrderService.createShopOrder(request, jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/user")
    public  ResponseEntity<List<ShopOrderResponse>> getAllUserShopOrder(@AuthenticationPrincipal Jwt jwt){
        List<ShopOrderResponse> shopOrders = shopOrderService.getUserShopOrdersWithProductItems(jwt);
        return ResponseEntity.status(HttpStatus.OK).body(shopOrders);
    }

    @GetMapping("/user/{shopOrderId}")
    public ResponseEntity<ShopOrderResponse> getUserShopOrderById(@PathVariable Integer shopOrderId, @AuthenticationPrincipal Jwt jwt){
        ShopOrderResponse ShopOrder = shopOrderService.getUserShopOrderById(shopOrderId, jwt);
        return ResponseEntity.status(HttpStatus.OK).body(ShopOrder);
    }

    @GetMapping("/by-payment-intent/{paymentIntentId}")
    public ResponseEntity<ShopOrderResponse> getByPaymentIntentId(@PathVariable String paymentIntentId, @AuthenticationPrincipal Jwt jwt) {
        ShopOrderResponse response = shopOrderService.getByPaymentIntentId(paymentIntentId, jwt);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{shopOrderId}")
    public ResponseEntity<ShopOrderResponse> updateShopOrderById(@PathVariable Integer shopOrderId, @RequestBody ShopOrderPaymentUpdateRequest shopOrderUpdateRequest, @AuthenticationPrincipal Jwt jwt){
        ShopOrderResponse order = shopOrderService.updateShopOrderById(shopOrderId,shopOrderUpdateRequest, jwt);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }
}
