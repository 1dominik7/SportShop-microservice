package com.ecommerce.user.clients;

import com.ecommerce.user.clients.dto.OrderLineResponse;
import com.ecommerce.user.clients.dto.SalesRatioStatistics;
import com.ecommerce.user.clients.dto.ShopOrderResponse;
import com.ecommerce.user.clients.dto.ShopOrderStatisticsResponse;
import com.ecommerce.user.statistics.dto.OrderStatusStatisticsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service", url = "${ORDER_SERVICE_URL}")
public interface ShopOrderClient {

    @GetMapping("/shop-order/user")
    List<ShopOrderResponse> getUserShopOrders(@RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/shop-order/statistics/totalOrders")
    ShopOrderStatisticsResponse getShopOrderIncomesAndTotalOrders(@RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/shop-order/statistics/topOrderStatuses")
    List<OrderStatusStatisticsResponse> getTopOrderStatuses(@RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/order-line/{orderLineId}")
    OrderLineResponse getOrderLineById(@PathVariable Integer orderLineId, @RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/order-line/get-by-ids")
    List<OrderLineResponse> getOrderLinesByIds(@RequestParam List<Integer> orderLinesIds);

    @GetMapping("/order-line/by-product-item-ids")
    List<OrderLineResponse> getOrderLinesByProductItemsIds(@RequestParam List<Integer> productItemIds);

    @GetMapping("/order-line/can-review/{orderLineId}")
    boolean canUserReviewOrderLine(@PathVariable Integer orderLineId,
                                   @RequestParam Integer productItemId,
                                   @RequestHeader("Authorization") String authorizationHeader);
}
