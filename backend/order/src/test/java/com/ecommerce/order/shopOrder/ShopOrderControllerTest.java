package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.dto.PaymentStatus;
import com.ecommerce.order.shopOrder.dto.ShopOrderPaymentUpdateRequest;
import com.ecommerce.order.shopOrder.dto.ShopOrderRequest;
import com.ecommerce.order.shopOrder.dto.ShopOrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest(classes = {
        ShopOrderController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class ShopOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopOrderService shopOrderService;


    @Test
    void ShopOrderController_CreateOrder_Success() throws Exception {

        Jwt jwt = mock(Jwt.class);

        ShopOrderRequest orderRequest = ShopOrderRequest.builder()
                .orderTotal(100.0)
                .finalOrderTotal(90.0)
                .shippingMethodId(1)
                .build();

        ShopOrderResponse order = ShopOrderResponse.builder()
                .id(1)
                .finalOrderTotal(90.0)
                .build();

        when(shopOrderService.createShopOrder(any(ShopOrderRequest.class), any(Jwt.class)))
                .thenReturn(order);

        mockMvc.perform(post("/shop-order/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(orderRequest))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.finalOrderTotal").value(90.0));
    }

    @Test
    void ShopOrderController_GetAllUserShopOrder_Success() throws Exception {

        Jwt jwt = mock(Jwt.class);

        ShopOrderResponse order1 = ShopOrderResponse.builder()
                .id(1)
                .finalOrderTotal(90.0)
                .build();

        ShopOrderResponse order2 = ShopOrderResponse.builder()
                .id(2)
                .finalOrderTotal(120.0)
                .build();

        when(shopOrderService.getUserShopOrdersWithProductItems(any(Jwt.class)))
                .thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/shop-order/user")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].finalOrderTotal").value(90.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].finalOrderTotal").value(120.0));
    }

    @Test
    void ShopOrderController_GetUserShopOrderById_Success() throws Exception {

        Jwt jwt = mock(Jwt.class);

        ShopOrderResponse response = ShopOrderResponse.builder().id(1).finalOrderTotal(90.0).build();

        when(shopOrderService.getUserShopOrderById(eq(1), any(Jwt.class)))
                .thenReturn(response);

        mockMvc.perform(get("/shop-order/user/{shopOrderId}", 1)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.finalOrderTotal").value(90.0));
    }

    @Test
    void ShopOrderController_GetByPaymentIntentId_Success() throws Exception {

        Jwt jwt = mock(Jwt.class);

        ShopOrderResponse response = ShopOrderResponse.builder().id(1).finalOrderTotal(90.0).build();

        when(shopOrderService.getByPaymentIntentId(eq("pi_123"), any(Jwt.class)))
                .thenReturn(response);

        mockMvc.perform(get("/shop-order/by-payment-intent/{paymentIntentId}", "pi_123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.finalOrderTotal").value(90.0));
    }

    @Test
    void ShopOrderController_UpdateShopOrderById_Success() throws Exception {

        Jwt jwt = mock(Jwt.class);

        ShopOrderPaymentUpdateRequest request = ShopOrderPaymentUpdateRequest.builder()
                .paymentStatus(PaymentStatus.SUCCEEDED)
                .build();

        ShopOrderResponse response = ShopOrderResponse.builder()
                .id(1)
                .paymentStatus(PaymentStatus.SUCCEEDED)
                .build();

        when(shopOrderService.updateShopOrderById(eq(1), any(ShopOrderPaymentUpdateRequest.class), any(Jwt.class)))
                .thenReturn(response);

        mockMvc.perform(put("/shop-order/{shopOrderId}", response.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCEEDED"));
    }
    }
