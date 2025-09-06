package com.ecommerce.payment.clients;

import com.ecommerce.payment.clients.dto.UserPaymentMethodResponse;
import com.ecommerce.payment.exceptions.ServiceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCallerService {

    private final UserClient userClient;

    @Retry(name = "userServiceRetry", fallbackMethod = "userServiceFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "userServiceFallback")
    @RateLimiter(name = "userService", fallbackMethod = "userServiceFallback")
    public UserPaymentMethodResponse getUserPaymentMethodById(String paymentMethodId, String jwt){
        return userClient.getUserPaymentMethodById(paymentMethodId, jwt);
    }

    public UserPaymentMethodResponse userServiceFallback(String paymentMethodId, String jwt, Throwable ex){
        log.error("Failed to get userService: ", ex.getMessage());
        throw new ServiceNotFoundException("user" ,"getUserPaymentMethodById" ,ex.getMessage());
    }
}
