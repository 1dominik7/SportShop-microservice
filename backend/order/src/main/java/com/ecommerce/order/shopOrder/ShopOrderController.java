package com.ecommerce.order.shopOrder;

import com.ecommerce.order.shopOrder.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("shop-order")
public class ShopOrderController {

    private final ShopOrderService shopOrderService;

    @PostMapping("/create")
    public ResponseEntity<ShopOrderResponse> createOrder(@RequestBody ShopOrderRequest request, @AuthenticationPrincipal Jwt jwt) {

        ShopOrderResponse order = shopOrderService.createShopOrder(request,jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/user")
    public  ResponseEntity<List<ShopOrderResponse>> getAllUserShopOrder(@AuthenticationPrincipal Jwt jwt){
        List<ShopOrderResponse> shopOrders = shopOrderService.getUserShopOrdersWithProductItems(jwt);
        return ResponseEntity.status(HttpStatus.OK).body(shopOrders);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ShopOrderResponse>> getUserShopOrderById(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required=false) String query,
            @RequestParam(required=false) String searchBy
    ){
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ShopOrderResponse> shopOrders = shopOrderService.getAllOrders(pageable, query,searchBy);
        return ResponseEntity.status(HttpStatus.OK).body(shopOrders);
    }

    @GetMapping("/user/{shopOrderId}")
    public ResponseEntity<ShopOrderResponse> getUserShopOrderById(@PathVariable Integer shopOrderId, @AuthenticationPrincipal Jwt jwt){
        ShopOrderResponse ShopOrder = shopOrderService.getUserShopOrderById(shopOrderId, jwt);
        return ResponseEntity.status(HttpStatus.OK).body(ShopOrder);
    }

    @GetMapping("/by-payment-intent/{paymentIntentId}")
    public ResponseEntity<ShopOrderResponse> getByPaymentIntentId(@PathVariable String paymentIntentId, @AuthenticationPrincipal Jwt jwt) {
        ShopOrderResponse response = shopOrderService.getByPaymentIntentId(paymentIntentId, jwt);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{shopOrderId}")
    public ResponseEntity<ShopOrderResponse> updateShopOrderById(@PathVariable Integer shopOrderId, @RequestBody ShopOrderPaymentUpdateRequest shopOrderUpdateRequest, @AuthenticationPrincipal Jwt jwt){
        ShopOrderResponse order = shopOrderService.updateShopOrderById(shopOrderId,shopOrderUpdateRequest, jwt);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @GetMapping("/statistics/totalOrders")
    public ResponseEntity<ShopOrderStatisticsResponse> getShopOrderIncomesAndTotalOrders(){
            ShopOrderStatisticsResponse shopOrderStatisticsResponse = shopOrderService.getShopOrderIncomesAndTotalOrders();
            return ResponseEntity.status(HttpStatus.OK).body(shopOrderStatisticsResponse);
    }

    @GetMapping("/statistics/topOrderStatuses")
    public ResponseEntity<List<OrderStatusStatisticsResponse>> getTopOrderStatuses(){
        List<OrderStatusStatisticsResponse> topOrderStatus = shopOrderService.getTopOrderStatuses();
        return ResponseEntity.status(HttpStatus.OK).body(topOrderStatus);
    }


    @GetMapping("/statistics/salesRatio")
    public ResponseEntity<SalesRatioStatistics> getStatistics(
            @RequestParam(name="month") Integer month,
            @RequestParam(name="year") Integer year
    ){
        SalesRatioStatistics getStatistics = shopOrderService.getSalesRatio(month,year);
        return ResponseEntity.status(HttpStatus.OK).body(getStatistics);
    }

    @GetMapping("/statistics/latestSales")
    public ResponseEntity<List<LatestSalesProductsResponse>> getLatestSales(
            @RequestParam(name="limit") Integer limit
    ){
        List<LatestSalesProductsResponse> getLatestSalesProducts = shopOrderService.getLatestProductSales(limit);
        return ResponseEntity.status(HttpStatus.OK).body(getLatestSalesProducts);
    }

    @GetMapping("/statistics/topProductSales")
    public ResponseEntity<List<TopProductSalesResponse>> getLatestSales(
            @RequestParam(name="month") Integer month,
            @RequestParam(name="year") Integer year,
            @RequestParam(name="limit") Integer limit
    ){
        List<TopProductSalesResponse> getLatestSalesProducts = shopOrderService.getTopProductSales(month,year,limit);
        return ResponseEntity.status(HttpStatus.OK).body(getLatestSalesProducts);
    }

    @PutMapping("/order-status/{orderStatusName}")
    public ResponseEntity<String> updateOrderStatusByIds(@PathVariable String orderStatusName, @RequestParam(name = "shopOrderIds") List<Integer> shopOrderIds){
        String result = shopOrderService.updateShopOrderStatus(orderStatusName,shopOrderIds);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

}
