package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.ProductItemCallerService;
import com.ecommerce.order.clients.UserCallerService;
import com.ecommerce.order.clients.dto.*;
import com.ecommerce.order.exceptions.APIException;
import com.ecommerce.order.exceptions.NotFoundException;
import com.ecommerce.order.orderLine.OrderLineRepository;
import com.ecommerce.order.orderLine.OrderLineService;
import com.ecommerce.order.orderStatus.OrderStatus;
import com.ecommerce.order.orderStatus.OrderStatusRepository;
import com.ecommerce.order.shippingMethod.ShippingMethod;
import com.ecommerce.order.shippingMethod.ShippingMethodRepository;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShopOrderServiceTest {

    @Mock
    private UserCallerService userCallerService;

    @Mock
    private ShopOrderRepository shopOrderRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private ShippingMethodRepository shippingMethodRepository;

    @Mock
    private ProductItemCallerService productItemCallerService;

    @InjectMocks
    private ShopOrderService shopOrderService;

    @Test
    void ShopOrderService_CreateShopOrder_Success() {
        Jwt jwt = mock(Jwt.class);

        ShoppingCartItemResponse cartItem = ShoppingCartItemResponse.builder()
                .productItemId(1)
                .qty(1)
                .productName("TestProduct")
                .build();

        ShoppingCartResponse cart = ShoppingCartResponse.builder()
                .shoppingCartItems(List.of(cartItem))
                .build();

        UserResponse user = UserResponse.builder()
                .id("user-123")
                .shoppingCart(cart)
                .addresses(List.of())
                .build();

        when(userCallerService.getUserProfile(any(Jwt.class))).thenReturn(user);

        ShippingMethod shippingMethod = ShippingMethod.builder()
                .id(1)
                .name("Test Method")
                .price(9.0)
                .build();
        when(shippingMethodRepository.findById(1)).thenReturn(Optional.of(shippingMethod));

        OrderStatus orderStatus = OrderStatus.builder()
                .id(1)
                .status("awaiting payment")
                .build();
        when(orderStatusRepository.findByStatus("awaiting payment")).thenReturn(Optional.of(orderStatus));

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(1)
                .productCode("Product123")
                .productId(1)
                .categoryId(1)
                .qtyInStock(1)
                .price(10.0)
                .build();

        ProductItemOneByColourResponse productResponse = new ProductItemOneByColourResponse();
        productResponse.setProductItemOneByColour(List.of(productItem));
        when(productItemCallerService.getProductItemByIdsToCreateOrder(List.of(1)))
                .thenReturn(List.of(productResponse));

        ShopOrder savedOrder = ShopOrder.builder()
                .id(1)
                .orderLines(List.of())
                .build();

        when(shopOrderRepository.save(any(ShopOrder.class))).thenReturn(savedOrder);

        ShopOrderRequest request = ShopOrderRequest.builder()
                .addressRequest(AddressRequest.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .street("Street 1")
                        .city("City")
                        .postalCode("00-000")
                        .country("PL")
                        .phoneNumber("123456789")
                        .build())
                .shippingMethodId(1)
                .orderTotal(200.0)
                .finalOrderTotal(180.0)
                .appliedDiscountValue(20)
                .build();

        ShopOrderResponse response = shopOrderService.createShopOrder(request, jwt);

        assertThat(response).isNotNull();
        verify(shopOrderRepository).save(any(ShopOrder.class));
        verify(userCallerService).getUserProfile(any(Jwt.class));
    }

    @Test
    void ShopOrderService_GetUserShopOrderById_Success() {
        Jwt jwt = mock(Jwt.class);

        UserResponse user = UserResponse.builder()
                .id("user-123")
                .addresses(List.of())
                .build();

        when(userCallerService.getUserProfile(any(Jwt.class))).thenReturn(user);

        ShopOrder order = ShopOrder.builder()
                .id(1)
                .userId("user-123")
                .orderLines(List.of())
                .build();
        when(shopOrderRepository.findById(1)).thenReturn(Optional.ofNullable(order));

        ShopOrderResponse response = shopOrderService.getUserShopOrderById(1, jwt);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        verify(shopOrderRepository).findById(1);
        verify(userCallerService).getUserProfile(any(Jwt.class));
    }

    @Test
    void ShopOrderService_GetUserShopOrderById_OrderNotFound() {

        Jwt jwt = mock(Jwt.class);

        UserResponse user = UserResponse.builder().id("user-123").build();
        when(userCallerService.getUserProfile(any(Jwt.class))).thenReturn(user);
        when(shopOrderRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopOrderService.getUserShopOrderById(1, jwt))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order");
    }

    @Test
    void ShopOrderService_GetUserShopOrderById_OrderBelongsToDiffUser() {

        Jwt jwt = mock(Jwt.class);

        UserResponse user = UserResponse.builder().id("user-123").build();
        when(userCallerService.getUserProfile(any(Jwt.class))).thenReturn(user);

        ShopOrder order = ShopOrder.builder()
                .id(1)
                .userId("user-999")
                .orderLines(List.of())
                .build();

        when(shopOrderRepository.findById(1)).thenReturn(Optional.ofNullable(order));

        assertThatThrownBy(() -> shopOrderService.getUserShopOrderById(1, jwt))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("This order does not belong to the user");
    }

    @Test
    void ShopOrderService_GetByPaymentIntentId_Success() {

        Jwt jwt = mock(Jwt.class);

        UserResponse user = UserResponse.builder().id("user-123").build();
        when(userCallerService.getUserProfile(any(Jwt.class))).thenReturn(user);

        ShopOrder order = ShopOrder.builder()
                .id(1)
                .userId("user-123")
                .orderLines(List.of())
                .build();

        when(shopOrderRepository.findByPaymentIntentIdAndUserId("pi_123", "user-123"))
                .thenReturn(order);

        ShopOrderResponse orderResponse = shopOrderService.getByPaymentIntentId("pi_123", jwt);

        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getId()).isEqualTo(1);
        verify(shopOrderRepository).findByPaymentIntentIdAndUserId("pi_123","user-123");
    }

    @Test
    void ShopOrderService_GetByPaymentIntentId_NotFound() {

        Jwt jwt = mock(Jwt.class);

        UserResponse user = UserResponse.builder().id("user-123").build();
        when(userCallerService.getUserProfile(any(Jwt.class))).thenReturn(user);

        when(shopOrderRepository.findByPaymentIntentIdAndUserId("pi_123", "user-123"))
                .thenReturn(null);

        assertThatThrownBy(() -> shopOrderService.getByPaymentIntentId("pi_123", jwt))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("PaymentIntent");
    }

    @Test
    void ShopOrderService_UpdateShopOrderById_NotFound() {

        Jwt jwt = mock(Jwt.class);
        UserResponse user = UserResponse.builder().id("user-123").build();
        when(userCallerService.getUserProfile(any(Jwt.class))).thenReturn(user);

        ShopOrder order = ShopOrder.builder()
                .id(1)
                .userId("user-123")
                .orderLines(List.of())
                .build();

        when(shopOrderRepository.findById(1)).thenReturn(Optional.ofNullable(order));

        ShopOrderPaymentUpdateRequest updateRequest = ShopOrderPaymentUpdateRequest.builder()
                .paymentId(123)
                .paymentStatus(PaymentStatus.SUCCEEDED)
                .paymentIntentId("pi_123")
                .paymentTransactionId("abc_456")
                .paymentMethodName("card")
                .paymentCreatedAt(LocalDateTime.now())
                .build();

        when(shopOrderRepository.save(any(ShopOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShopOrderResponse response = shopOrderService.updateShopOrderById(1, updateRequest, jwt);

        assertThat(response).isNotNull();
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        verify(shopOrderRepository).save(any(ShopOrder.class));
    }
}
