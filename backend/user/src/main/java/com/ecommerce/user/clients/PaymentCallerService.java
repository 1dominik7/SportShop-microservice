package com.ecommerce.user.clients;

import com.ecommerce.user.clients.dto.PaymentTypeResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallerService {
    private final PaymentClient paymentClient;

    @Retry(name = "paymentService", fallbackMethod = "paymentServiceFallback")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentServiceFallback")
    @RateLimiter(name = "paymentService", fallbackMethod = "paymentServiceFallback")
    public List<PaymentTypeResponse> getAllPaymentMethods() {
        return paymentClient.paymentTypeResponse();
    }

    public List<PaymentTypeResponse> paymentServiceFallback(Throwable ex){
        log.error("Failed to getAllPaymentMethods, payment service, error: ", ex.getMessage());
        return Collections.emptyList();
    }
}
