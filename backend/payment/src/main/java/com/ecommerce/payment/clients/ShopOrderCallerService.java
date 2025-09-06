package com.ecommerce.payment.clients;

import com.ecommerce.payment.clients.dto.ShopOrderRequest;
import com.ecommerce.payment.clients.dto.ShopOrderResponse;
import com.ecommerce.payment.clients.dto.ShopOrderPaymentUpdateRequest;
import com.ecommerce.payment.exceptions.ServiceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopOrderCallerService {

    private final ShopOrderClient shopOrderClient;

    @Retry(name = "shopOrderServiceRetry", fallbackMethod = "shopOrderGetOrderIdFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "shopOrderGetOrderIdFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "shopOrderGetOrderIdFallback")
    public ShopOrderResponse getUserShopOrderById(Integer shopOrderId, Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.getUserShopOrderById(shopOrderId, token);
    }

    public ShopOrderResponse shopOrderGetOrderIdFallback(Integer shopOrderId, Jwt jwt, Throwable ex) {
        log.error("Failed to getOrderId, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("shopOrder", "getUserShopOrderById", ex.getMessage());
    }

    @Retry(name = "shopOrderServiceRetry", fallbackMethod = "shopOrderGetPaymentIntentIdFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "shopOrderGetPaymentIntentIdFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "shopOrderGetPaymentIntentIdFallback")
    public ShopOrderResponse getByPaymentIntentId(String paymentIntentId, Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.getByPaymentIntentId(paymentIntentId, token);
    }

    public ShopOrderResponse shopOrderGetPaymentIntentIdFallback(String paymentIntentId, Jwt jwt, Throwable ex) {
        log.error("Failed to getPaymentIntentId, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("shopOrder", "getByPaymentIntentId", ex.getMessage());
    }

    @Retry(name = "shopOrderServiceRetry", fallbackMethod = "shopOrderCreateShopOrderFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "shopOrderCreateShopOrderFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "shopOrderCreateShopOrderFallback")
    public ShopOrderResponse createShopOrder(ShopOrderRequest shopOrder, Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.createShopOrder(shopOrder, token);
    }

    public ShopOrderResponse shopOrderCreateShopOrderFallback(ShopOrderRequest shopOrder, Jwt jwt, Throwable ex) {
        log.error("Failed to createShopOrder, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("shopOrder", "createShopOrder", ex.getMessage());
    }

    @Retry(name = "shopOrderServiceRetry", fallbackMethod = "createShopOrderByKafkaFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "createShopOrderByKafkaFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "createShopOrderByKafkaFallback")
    public ShopOrderResponse createShopOrderByKafka(ShopOrderRequest shopOrder) {
        return shopOrderClient.createShopOrderByKafka(shopOrder);
    }

    public ShopOrderResponse createShopOrderByKafkaFallback(ShopOrderRequest shopOrder, Throwable ex) {
        log.error("Failed to createShopOrder, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("shopOrder", "createShopOrder", ex.getMessage());
    }

    @Retry(name = "shopOrderServiceRetry", fallbackMethod = "updateShopOrderFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "updateShopOrderFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "updateShopOrderFallback")
    public ShopOrderResponse updateShopOrder(Integer shopOrderId, ShopOrderPaymentUpdateRequest shopOrderUpdateRequest, Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.updateShopOrder(shopOrderId, shopOrderUpdateRequest, token);
    }

    public ShopOrderResponse updateShopOrderFallback(Integer shopOrderId, ShopOrderPaymentUpdateRequest shopOrderUpdateRequest, Jwt jwt, Throwable ex) {
        log.error("Failed to updateShopOrderFallback, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("shopOrder", "updateShopOrderFallback", ex.getMessage());
    }
}
