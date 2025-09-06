package com.ecommerce.order.orderStatus;

import com.ecommerce.order.exceptions.APIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderStatusServiceTest {

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @InjectMocks
    private OrderStatusService orderStatusService;

    @Test
    void OrderStatusService_CreateOrderStatus_Success() {

        OrderStatusRequest orderStatusRequest = OrderStatusRequest.builder()
                .status("In Order")
                .build();

        OrderStatus orderStatusResponse = OrderStatus.builder()
                .id(1)
                .status("In Order")
                .build();

        when(orderStatusRepository.findByStatus(orderStatusRequest.getStatus())).thenReturn(Optional.empty());
        when(orderStatusRepository.save(any(OrderStatus.class))).thenReturn(orderStatusResponse);

        OrderStatusResponse response = orderStatusService.createOrderStatus(orderStatusRequest);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("In Order", response.getStatus());

        verify(orderStatusRepository).findByStatus("In Order");
        verify(orderStatusRepository).save(any(OrderStatus.class));
    }


    @Test
    void OrderStatusService_CreateOrderStatus_StatusAlreadyExists() {

        OrderStatusRequest orderStatusRequest = OrderStatusRequest.builder()
                .status("In Order")
                .build();

        OrderStatus orderStatusResponse = OrderStatus.builder()
                .id(1)
                .status("In Order")
                .build();

        when(orderStatusRepository.findByStatus(orderStatusRequest.getStatus())).thenReturn(Optional.of(orderStatusResponse));

        APIException exception = assertThrows(APIException.class, () -> {
            orderStatusService.createOrderStatus(orderStatusRequest);
        });

        assertEquals("Order status with this name exists!", exception.getMessage());

        verify(orderStatusRepository).findByStatus("In Order");
        verify(orderStatusRepository, never()).save(any());
    }

    @Test
    void OrderStatusService_GetAllOrderStatuses_Success() {

        OrderStatus orderStatusResponse1 = OrderStatus.builder()
                .id(1)
                .status("In Order")
                .build();

        OrderStatus orderStatusResponse2 = OrderStatus.builder()
                .id(2)
                .status("In Delivery")
                .build();

        when(orderStatusRepository.findAll()).thenReturn(List.of(orderStatusResponse1,orderStatusResponse2));

        List<OrderStatusResponse> response = orderStatusService.getAllOrderStatuses();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("In Order", response.get(0).getStatus());
        assertEquals("In Delivery", response.get(1).getStatus());

        verify(orderStatusRepository).findAll();
    }

    @Test
    void OrderStatusService_DeleteOrderStatus_Success() {
        OrderStatus orderStatusResponse1 = OrderStatus.builder()
                .id(1)
                .status("In Order")
                .build();

        when(orderStatusRepository.findById(1)).thenReturn(Optional.of(orderStatusResponse1));

        orderStatusService.deleteOrderStatus(1);

        verify(orderStatusRepository).findById(1);
        verify(orderStatusRepository).delete(orderStatusResponse1);
    }

}
