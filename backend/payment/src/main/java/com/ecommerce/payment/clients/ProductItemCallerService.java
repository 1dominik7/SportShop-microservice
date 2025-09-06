package com.ecommerce.payment.clients;

import com.ecommerce.payment.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.payment.clients.dto.ProductStockUpdateRequest;
import com.ecommerce.payment.exceptions.ServiceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductItemCallerService {

    private final ProductItemClient productItemClient;

    @Retry(name = "productServiceRetry", fallbackMethod = "productItemUpdateStockFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "productItemUpdateStockFallback")
    @RateLimiter(name = "productService", fallbackMethod = "productItemUpdateStockFallback")
    public void updateProductItemStock(List<ProductStockUpdateRequest> updates, Jwt jwt){
        String token = "Bearer " + jwt.getTokenValue();
        productItemClient.updateProductItemStock(updates, token);
    }

    public void productItemUpdateStockFallback(List<ProductStockUpdateRequest> updates, Jwt jwt, Throwable ex){
        log.error("Failed to updateStock, product service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("product" ,"updateProductItemStock" ,ex.getMessage());
    }

    @Retry(name = "productServiceRetry", fallbackMethod = "getProductItemsByIdsFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductItemsByIdsFallback")
    @RateLimiter(name = "productService", fallbackMethod = "getProductItemsByIdsFallback")
    public List<ProductItemOneByColourResponse> getProductItemsByIds(List<Integer> productItemIds){
        return productItemClient.getProductItemsByIds(productItemIds);
    }

    public List<ProductItemOneByColourResponse> getProductItemsByIdsFallback(List<Integer> productItemIds, Throwable ex){
        log.error("Failed to getProductItemsByIds, product service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("product" ,"getProductItemsByIds" ,ex.getMessage());
    }
}
