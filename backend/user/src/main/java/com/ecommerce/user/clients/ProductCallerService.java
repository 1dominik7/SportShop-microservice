package com.ecommerce.user.clients;

import com.ecommerce.user.clients.dto.ProductItemOneByColour;
import com.ecommerce.user.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.user.clients.dto.ProductResponseGetById;
import com.ecommerce.user.exceptions.ServiceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCallerService {
    private final ProductClient productClient;

    @Retry(name = "productService", fallbackMethod = "getProductItemByIdServiceFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductItemByIdServiceFallback")
    @RateLimiter(name = "productService", fallbackMethod = "getProductItemByIdServiceFallback")
    public ProductItemOneByColourResponse getProductItemById(Integer productItemId, String colour) {
        return productClient.getProductItemById(productItemId, colour);
    }

    public ProductItemOneByColourResponse getProductItemByIdServiceFallback(Integer productItemId, Throwable ex){
        log.error("Failed to getProductItemById, product service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("product", "getProductItemById", ex.getMessage());
    }

    @Retry(name = "productService", fallbackMethod = "getProductItemByIdsFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductItemByIdsFallback")
    @RateLimiter(name = "productService", fallbackMethod = "getProductItemByIdsFallback")
    public List<ProductItemOneByColourResponse> getProductItemByIds(List<Integer> productItemIds) {
        return productClient.getProductItemByIds(productItemIds);
    }

    public List<ProductItemOneByColour> getProductItemByIdsFallback(List<Integer> productItemIds, Throwable ex) {
        log.error("Failed to getProductItemById, product service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("product", "getProductItemById", ex.getMessage());
    }

    @Retry(name = "productService", fallbackMethod = "getProductByIdServiceFallback")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdServiceFallback")
    @RateLimiter(name = "productService", fallbackMethod = "getProductByIdServiceFallback")
    public ProductResponseGetById getProductById(Integer id) {
        return productClient.getProductById(id);
    }

    public ProductResponseGetById getProductByIdServiceFallback(Integer id, Throwable ex){
        log.error("Failed to getProductById, product service, error: ", ex.getMessage());
        return new ProductResponseGetById();
    }
}
