package com.ecommerce.order.clients;

import com.ecommerce.order.clients.dto.UserResponse;
import com.ecommerce.order.exceptions.ServiceNotFoundException;
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

    @Retry(name = "retryBreaker", fallbackMethod = "getUserByIdFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @RateLimiter(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(String userId) {
        return userClient.getUserById(userId);
    }

    public UserResponse getUserByIdFallback(String userId, Throwable ex){

        log.error("Failed to getUserByIdFallback, user service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("user", "getUserByIdFallback", ex.getMessage());
    }
}
