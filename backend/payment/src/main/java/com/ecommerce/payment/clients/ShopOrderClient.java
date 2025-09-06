package com.ecommerce.payment.clients;

import com.ecommerce.payment.clients.dto.ShopOrderRequest;
import com.ecommerce.payment.clients.dto.ShopOrderResponse;
import com.ecommerce.payment.clients.dto.ShopOrderPaymentUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="order-service", url = "${ORDER_SERVICE_URL}")
public interface ShopOrderClient {

    @GetMapping("/shop-order/user/{shopOrderId}")
    ShopOrderResponse getUserShopOrderById(@PathVariable Integer shopOrderId, @RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/by-payment-intent/{paymentIntentId}")
    ShopOrderResponse getByPaymentIntentId(@PathVariable String paymentIntentId,
                                           @RequestHeader("Authorization") String authorizationHeader);

    @PostMapping("/shop-order/create")
    ShopOrderResponse createShopOrder(@RequestBody ShopOrderRequest shopOrder, @RequestHeader("Authorization") String authorizationHeader);

    @PostMapping("/shop-order")
    ShopOrderResponse createShopOrderByKafka(@RequestBody ShopOrderRequest shopOrder);

    @PutMapping("/shop-order/{shopOrderId}")
    ShopOrderResponse updateShopOrder(@PathVariable Integer shopOrderId, @RequestBody ShopOrderPaymentUpdateRequest shopOrderUpdateRequest, @RequestHeader("Authorization") String authorizationHeader);

}
