package com.ecommerce.order.clients;

import com.ecommerce.order.clients.dto.*;
import com.ecommerce.order.exceptions.ServiceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductItemCallerService {

    private final ProductItemClient productItemClient;

    @Retry(name = "retryBreaker", fallbackMethod = "getProductItemByIdServiceFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductItemByIdServiceFallback")
    @RateLimiter(name = "productService", fallbackMethod = "getProductItemByIdServiceFallback")
    public ProductItemResponse getProductItemById(Integer productItemId) {
        return productItemClient.getProductItemById(productItemId);
    }

    public ProductItemResponse getProductItemByIdServiceFallback(Integer productItemId, Throwable ex) {
        log.error("Failed to getProductItemByIdServiceFallback, product service, error: ", ex.getMessage());
        return ProductItemResponse.builder()
                .id(productItemId)
                .price(0.00)
                .discount(0)
                .productCode("Unknown")
                .qtyInStock(0)
                .productId(0)
                .variationOptions(Collections.emptyList())
                .productImages(Collections.emptySet())
                .productName("Unknown Product")
                .productDescription("Unknown Description")
                .build();
    }

    @Retry(name = "retryBreaker", fallbackMethod = "getProductItemByIdsToCreateOrderFallbackFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductItemByIdsToCreateOrderFallbackFallback")
    @RateLimiter(name = "productService", fallbackMethod = "getProductItemByIdsToCreateOrderFallbackFallback")
    public List<ProductItemOneByColourResponse> getProductItemByIdsToCreateOrder(List<Integer> productItemIds) {
        return productItemClient.getProductItemByIdsToCreateOrder(productItemIds);
    }

    public List<ProductItemOneByColourResponse> getProductItemByIdsToCreateOrderFallback(List<Integer> productItemIds, Throwable ex) {
        log.error("Failed to getProductItemByIdsToCreateOrderFallback, product service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("product", "getProductItemByIdsToCreateOrderFallback", ex.getMessage());
    }

    @Retry(name = "retryBreaker", fallbackMethod = "getProductItemByIdsToOrdersFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductItemByIdsToOrdersFallback")
    @RateLimiter(name = "productService", fallbackMethod = "getProductItemByIdsToOrdersFallback")
    public List<ProductItemToOrderResponse> getProductItemByIdsToOrders(List<Integer> productItemIds) {
        return productItemClient.getProductItemByIdsToOrders(productItemIds);
    }

    public List<ProductItemToOrderResponse> getProductItemByIdsToOrdersFallback(List<Integer> productItemIds, Throwable ex) {
        log.error("Failed to getProductItemByIdsToOrdersFallback, product service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("product", "getProductItemByIdsToOrdersFallback", ex.getMessage());
    }


}
