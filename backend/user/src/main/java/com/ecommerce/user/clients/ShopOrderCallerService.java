package com.ecommerce.user.clients;

import com.ecommerce.user.clients.dto.OrderLineResponse;
import com.ecommerce.user.clients.dto.SalesRatioStatistics;
import com.ecommerce.user.clients.dto.ShopOrderResponse;
import com.ecommerce.user.clients.dto.ShopOrderStatisticsResponse;
import com.ecommerce.user.exceptions.ServiceNotFoundException;
import com.ecommerce.user.statistics.dto.OrderStatusStatisticsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopOrderCallerService {

    private final ShopOrderClient shopOrderClient;

    @Retry(name = "shopOrderService", fallbackMethod = "shopOrderServiceFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "shopOrderServiceFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "shopOrderServiceFallback")
    public List<ShopOrderResponse> getUserShopOrders(Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.getUserShopOrders(token);
    }

    public List<ShopOrderResponse> shopOrderServiceFallback(String jwt, Throwable ex){
        log.error("Failed to getUserShopOrders, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("Shop Order", "getUserShopOrders", ex.getMessage());
    }

    @Retry(name = "shopOrderService", fallbackMethod = "getShopOrderIncomesAndTotalOrdersServiceFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "getShopOrderIncomesAndTotalOrdersServiceFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "getShopOrderIncomesAndTotalOrdersServiceFallback")
    public ShopOrderStatisticsResponse getShopOrderIncomesAndTotalOrders(Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.getShopOrderIncomesAndTotalOrders(token);
    }

    public List<ShopOrderResponse> getShopOrderIncomesAndTotalOrdersServiceFallback(Throwable ex){
        log.error("Failed to getShopOrderIncomesAndTotalOrders, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("Shop Order", "getShopOrderIncomesAndTotalOrders", ex.getMessage());
    }

    @Retry(name = "shopOrderService", fallbackMethod = "getShopOrderIncomesAndTotalOrdersServiceFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "getShopOrderIncomesAndTotalOrdersServiceFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "getShopOrderIncomesAndTotalOrdersServiceFallback")
    public List<OrderStatusStatisticsResponse> getTopOrderStatuses(Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.getTopOrderStatuses(token);
    }

    public List<OrderStatusStatisticsResponse> getTopOrderStatusesServiceFallback(Throwable ex){
        log.error("Failed to getTopOrderStatusesServiceFallback, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("Shop Order", "getTopOrderStatusesServiceFallback", ex.getMessage());
    }

    @Retry(name = "shopOrderService", fallbackMethod = "getOrderLineByIdServiceFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "getOrderLineByIdServiceFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "getOrderLineByIdServiceFallback")
    public OrderLineResponse getOrderLineById(Integer orderLineId, Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.getOrderLineById(orderLineId, token);
    }

    public OrderLineResponse getOrderLineByIdServiceFallback(Integer orderLineId, Jwt jwt, Throwable ex){
        log.error("Failed to getOrderLineById, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("Shop Order", "getOrderLineById", ex.getMessage());
    }

    @Retry(name = "shopOrderService", fallbackMethod = "getOrderLinesByIdsServiceFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "getOrderLinesByIdsServiceFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "getOrderLinesByIdsServiceFallback")
    public List<OrderLineResponse> getOrderLinesByIds(List<Integer> orderLinesIds) {
        return shopOrderClient.getOrderLinesByIds(orderLinesIds);
    }

    public List<OrderLineResponse> getOrderLinesByIdsServiceFallback(List<Integer> orderLinesIds, Throwable ex){
        log.error("Failed to getOrderLinesByIds, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("Shop Order", "getOrderLinesByIds", ex.getMessage());
    }

    @Retry(name = "shopOrderService", fallbackMethod = "getOrderLinesByProductItemsIdsServiceFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "getOrderLinesByProductItemsIdsServiceFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "getOrderLinesByProductItemsIdsServiceFallback")
    public List<OrderLineResponse> getOrderLinesByProductItemsIds(List<Integer> productItemIds) {
        return shopOrderClient.getOrderLinesByProductItemsIds(productItemIds);
    }

    public List<OrderLineResponse> getOrderLinesByProductItemsIdsServiceFallback(List<Integer> productItemIds, Throwable ex){
        log.error("Failed to getOrderLinesByProductItemsIdsServiceFallback, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("Shop Order", "getOrderLinesByProductItemsIdsServiceFallback", ex.getMessage());
    }

    @Retry(name = "shopOrderService", fallbackMethod = "canUserReviewOrderLineServiceFallback")
    @CircuitBreaker(name = "shopOrderService", fallbackMethod = "canUserReviewOrderLineServiceFallback")
    @RateLimiter(name = "shopOrderService", fallbackMethod = "canUserReviewOrderLineServiceFallback")
    public boolean canUserReviewOrderLine(Integer orderLineId, Integer productItemId, Jwt jwt) {
        String token = "Bearer " + jwt.getTokenValue();
        return shopOrderClient.canUserReviewOrderLine(orderLineId,productItemId,token);
    }

    public boolean canUserReviewOrderLineServiceFallback(Integer orderLineId, Integer productItemId, Jwt jwt, Throwable ex){
        log.error("Failed to canUserReviewOrderLineServiceFallback, shopOrder service, error: ", ex.getMessage());
        throw new ServiceNotFoundException("Shop Order", "canUserReviewOrderLineServiceFallback", ex.getMessage());
    }

}
