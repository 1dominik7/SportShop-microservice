package com.ecommerce.marketing.clients;

import com.ecommerce.marketing.clients.dto.DiscountCodeRequest;
import com.ecommerce.marketing.clients.dto.DiscountCodeResponse;
import com.ecommerce.marketing.exceptions.ServiceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCallerService {

    private final UserClient userClient;

    @Retry(name = "retryBreaker", fallbackMethod = "userServiceFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "userServiceFallback")
    @RateLimiter(name = "userService", fallbackMethod = "userServiceFallback")
    public DiscountCodeResponse createDiscountCode(DiscountCodeRequest discountCodeRequest) {
        return userClient.createDiscountCode(discountCodeRequest);
    }

    public DiscountCodeResponse userServiceFallback(DiscountCodeRequest discountCodeRequest, Throwable ex){

        throw new ServiceNotFoundException("user", "createDiscountCode", ex.getMessage());
    }

}
