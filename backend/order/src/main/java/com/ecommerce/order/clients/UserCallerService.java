package com.ecommerce.order.clients;

import com.ecommerce.order.clients.dto.UserResponse;
import com.ecommerce.order.exceptions.ServiceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCallerService {
    private final UserClient userClient;

    @Retry(name = "retryBreaker", fallbackMethod = "userServiceFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "userServiceFallback")
    @RateLimiter(name = "userService", fallbackMethod = "userServiceFallback")
    public UserResponse getUserProfile(Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return userClient.getUserProfile(token);
    }

    public UserResponse userServiceFallback(Jwt jwt, Throwable ex){
        log.error("Failed to getUserProfile, user service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("user", "getUserProfile", ex.getMessage());
    }

    @Retry(name = "retryBreaker", fallbackMethod = "clearUserCartServiceFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "clearUserCartServiceFallback")
    @RateLimiter(name = "userService", fallbackMethod = "clearUserCartServiceFallback")
    public void clearUserCart(Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        userClient.clearUseCart(token);
    }

    public void clearUserCartServiceFallback(Jwt jwt, Throwable ex){
        log.error("Failed to clear user cart, user service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("user", "clearUserCart", ex.getMessage());
    }

    @Retry(name = "retryBreaker", fallbackMethod = "getUserByIdeFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdeFallback")
    @RateLimiter(name = "userService", fallbackMethod = "getUserByIdeFallback")
    public UserResponse getUserById(String Id) {
        return userClient.getUserById(Id);
    }

    public UserResponse getUserByIdeFallback(String Id, Throwable ex){
        log.error("Failed to getUserById, user service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("user", "getUserById", ex.getMessage());
    }
}
