package com.ecommerce.order.orderStatus;

import com.ecommerce.order.exceptions.APIException;
import com.ecommerce.order.exceptions.NotFoundException;
import com.ecommerce.order.shippingMethod.ShippingMethod;
import com.ecommerce.order.shippingMethod.ShippingMethodRequest;
import com.ecommerce.order.shippingMethod.ShippingMethodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;

    @Transactional
    public OrderStatusResponse createOrderStatus(OrderStatusRequest orderStatusRequest) {
        orderStatusRepository.findByStatus(orderStatusRequest.getStatus()).ifPresent(existing -> {
            throw new APIException("Order status with this name exists!");
        });
        OrderStatus newStatus = OrderStatus.builder()
                .status(orderStatusRequest.getStatus())
                .build();
        OrderStatus saved = orderStatusRepository.save(newStatus);

        return OrderStatusResponse.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .build();
    }

    public List<OrderStatusResponse> getAllOrderStatuses() {
        List<OrderStatus> orderStatuses = orderStatusRepository.findAll();

        return orderStatuses.stream().map(os ->
                OrderStatusResponse.builder()
                        .id(os.getId())
                        .status(os.getStatus())
                        .build()).collect(Collectors.toList());
    }

    @Transactional
    public OrderStatusResponse updateOrderStatus(OrderStatusRequest orderStatusRequest, Integer orderStatusId) {
        OrderStatus orderStatusResponse = orderStatusRepository.findById(orderStatusId).orElseThrow(() -> new NotFoundException("OrderStatus", Optional.of(orderStatusId.toString())));

        orderStatusResponse.setStatus(orderStatusRequest.getStatus());;

        OrderStatus updated = orderStatusRepository.save(orderStatusResponse);

        return OrderStatusResponse.builder()
                .id(updated.getId())
                .status(updated.getStatus())
                .build();
    }

    @Transactional
    public void deleteOrderStatus(Integer orderStatusId) {
        OrderStatus orderStatus = orderStatusRepository.findById(orderStatusId).orElseThrow(() ->
                new NotFoundException("OrderStatus", Optional.of(orderStatusId.toString())));
        orderStatusRepository.delete(orderStatus);
    }
}
