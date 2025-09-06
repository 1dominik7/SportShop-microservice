package com.ecommerce.order.orderLine;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.order.clients.UserCallerService;
import com.ecommerce.order.clients.dto.UserResponse;
import com.ecommerce.order.exceptions.NotFoundException;
import com.ecommerce.order.shippingMethod.ShippingMethodRepository;
import com.ecommerce.order.shippingMethod.ShippingMethodService;
import com.ecommerce.order.shopOrder.ShopOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderLineServiceTest {

    @Mock
    private UserCallerService userCallerService;

    @Mock
    private OrderLineRepository orderLineRepository;

    @InjectMocks
    private OrderLineService orderLineService;

    @Test
    void OrderLineService_GetOrderLinesByIds_Success() {

        OrderLine orderLine1 = OrderLine.builder()
                .id(1)
                .productItemId(1)
                .productName("Product1")
                .qty(1)
                .price(10.0)
                .build();

        OrderLine orderLine2 = OrderLine.builder()
                .id(2)
                .productItemId(2)
                .productName("Product2")
                .qty(2)
                .price(11.0)
                .build();

        List<Integer> ids = List.of(1, 2);

        when(orderLineRepository.findAllById(ids)).thenReturn(List.of(orderLine1, orderLine2));

        List<OrderLineResponse> result = orderLineService.getOrderLinesByIds(ids);

        assertNotNull(result);
        assertEquals(2, result.size());

        OrderLineResponse res1 = result.get(0);
        assertEquals(1, res1.getId());
        assertEquals("Product1", res1.getProductName());
        assertEquals(1, res1.getProductItemId());
        assertEquals(1, res1.getQty());
        assertEquals(10.0, res1.getPrice());

        OrderLineResponse res2 = result.get(1);
        assertEquals(2, res2.getId());
        assertEquals("Product2", res2.getProductName());
        assertEquals(2, res2.getProductItemId());
        assertEquals(2, res2.getQty());
        assertEquals(11.0, res2.getPrice());
    }

    @Test
    void OrderLineService_GetOrderLineById_Success() throws AccessDeniedException {
        Integer orderLineId = 1;
        String userId = "user-123";
        Jwt jwt = mock(Jwt.class);

        UserResponse currentUser = UserResponse.builder()
                .id(userId)
                .build();

        ShopOrder shopOrder = ShopOrder.builder()
                .userId(userId)
                .orderTotal(10.0)
                .build();

        OrderLine orderLine = OrderLine.builder()
                .id(1)
                .productItemId(1)
                .productName("Product1")
                .qty(1)
                .price(10.0)
                .shopOrder(shopOrder)
                .build();


        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(userCallerService.getUserProfile(jwt)).thenReturn(currentUser);

        OrderLineResponse response = orderLineService.getOrderLineById(orderLineId, jwt);

        assertNotNull(response);
        assertEquals(orderLineId, response.getId());
        assertEquals("Product1", response.getProductName());
        assertEquals(1, response.getProductItemId());
        assertEquals(1, response.getQty());
        assertEquals(10.0, response.getPrice());

        verify(orderLineRepository).findById(orderLineId);
        verify(userCallerService).getUserProfile(jwt);
    }


    @Test
    void OrderLineService_GetOrderLineById_OrderLineMissing() throws AccessDeniedException {
        Integer orderLineId = 1;
        Jwt jwt = mock(Jwt.class);

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> {
            orderLineService.getOrderLineById(orderLineId, jwt);
        });

        assertEquals("OrderLine not found with 1", ex.getMessage());

        verify(orderLineRepository).findById(orderLineId);
        verifyNoInteractions(userCallerService);
    }

    @Test
    void OrderLineService_GetOrderLineById_UserIsNotOwner() {
        Integer orderLineId = 1;
        Jwt jwt = mock(Jwt.class);
        String currentUserId = "user-123";
        String ownerUserId = "user-345";

        UserResponse currentUser = UserResponse.builder()
                .id(currentUserId)
                .build();

        ShopOrder shopOrder = ShopOrder.builder()
                .userId(ownerUserId)
                .orderTotal(10.0)
                .build();

        OrderLine orderLine = OrderLine.builder()
                .id(1)
                .productItemId(1)
                .productName("Product1")
                .qty(1)
                .price(10.0)
                .shopOrder(shopOrder)
                .build();

        when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));
        when(userCallerService.getUserProfile(jwt)).thenReturn(currentUser);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            orderLineService.getOrderLineById(orderLineId, jwt);
        });

        assertEquals("You don't have permission to view this order line", ex.getMessage());

        verify(orderLineRepository).findById(orderLineId);
        verify(userCallerService).getUserProfile(jwt);
    }

    @Test
    void OrderLineService_GetOrderLineByProductItemIds_Success() {
        String currentUserId = "user-123";

        ShopOrder shopOrder = ShopOrder.builder()
                .userId(currentUserId)
                .orderTotal(10.0)
                .build();

        OrderLine orderLine1 = OrderLine.builder()
                .id(1)
                .productItemId(1)
                .productName("Product1")
                .qty(1)
                .price(10.0)
                .shopOrder(shopOrder)
                .build();

        OrderLine orderLine2 = OrderLine.builder()
                .id(2)
                .productItemId(2)
                .productName("Product2")
                .qty(2)
                .price(11.0)
                .shopOrder(shopOrder)
                .build();

        List<Integer> productItemIds = List.of(1, 2);

        when(orderLineRepository.findByProductItemIdIn(productItemIds))
                .thenReturn(List.of(orderLine1, orderLine2));

        List<OrderLineResponse> responses = orderLineService.getOrderLineByProductItemIds(productItemIds);

        assertEquals(2, responses.size());
        assertEquals("Product1", responses.get(0).getProductName());
        assertEquals("Product2", responses.get(1).getProductName());

        verify(orderLineRepository).findByProductItemIdIn(productItemIds);
    }

    @Test
    void OrderLineService_GetOrderLineByProductItemIds_NoOrderLinesFound() {
        List<Integer> productItemIds = List.of(2);

        when(orderLineRepository.findByProductItemIdIn(productItemIds)).thenReturn(Collections.emptyList());

        List<OrderLineResponse> responses = orderLineService.getOrderLineByProductItemIds(productItemIds);

        assertTrue(responses.isEmpty());

        verify(orderLineRepository).findByProductItemIdIn(productItemIds);
    }

    @Test
    void OrderLineService_CanUserReviewOrderLine_Success() {
        Integer orderLineId = 1;
        Integer productItemId = 1;
        Jwt jwt = mock(Jwt.class);
        String currentUserId = "user-123";

        UserResponse currentUser = UserResponse.builder()
                .id(currentUserId)
                .build();

        when(userCallerService.getUserProfile(jwt)).thenReturn(currentUser);
        when(orderLineRepository.existsByIdAndProductItemIdAndShopOrder_UserId(orderLineId, productItemId, currentUserId)).thenReturn(true);

        boolean result = orderLineService.canUserReviewOrderLine(orderLineId, productItemId, jwt);

        assertTrue(result);
        verify(orderLineRepository).existsByIdAndProductItemIdAndShopOrder_UserId(orderLineId, productItemId, currentUserId);
    }

    @Test
    void OrderLineService_CanUserReviewOrderLine_OrderLineDoesNotBelongToUser() {
        Integer orderLineId = 1;
        Integer productItemId = 1;
        Jwt jwt = mock(Jwt.class);
        String currentUserId = "user-123";

        UserResponse currentUser = UserResponse.builder()
                .id(currentUserId)
                .build();

        when(userCallerService.getUserProfile(jwt)).thenReturn(currentUser);
        when(orderLineRepository.existsByIdAndProductItemIdAndShopOrder_UserId(orderLineId, productItemId, currentUserId)).thenReturn(false);

        boolean result = orderLineService.canUserReviewOrderLine(orderLineId, productItemId, jwt);

        assertFalse(result);
    }
}