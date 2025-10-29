package com.ecommerce.order.orderStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("order-status")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @PostMapping("/create")
    public ResponseEntity<OrderStatusResponse> createOrderStatus(@RequestBody OrderStatusRequest orderStatusRequest) {
        OrderStatusResponse orderStatusResponse = orderStatusService.createOrderStatus(orderStatusRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderStatusResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderStatusResponse>> getAllOrderStatuses() {
        List<OrderStatusResponse> orderStatuses = orderStatusService.getAllOrderStatuses();
        return ResponseEntity.status(HttpStatus.OK).body(orderStatuses);
    }

    @PutMapping("/{orderStatusId}")
    public ResponseEntity<OrderStatusResponse> updateOrderStatus(@RequestBody OrderStatusRequest orderStatusRequest, @PathVariable Integer orderStatusId) {
        OrderStatusResponse orderStatus = orderStatusService.updateOrderStatus(orderStatusRequest, orderStatusId);
        return ResponseEntity.status(HttpStatus.OK).body(orderStatus);
    }

    @DeleteMapping("/{orderStatusId}")
    public ResponseEntity<String> deleteOrderStatus(@PathVariable Integer orderStatusId) {
        orderStatusService.deleteOrderStatus(orderStatusId);
        return ResponseEntity.status(HttpStatus.OK).body("Order status has been successfully deleted!");
    }
}
