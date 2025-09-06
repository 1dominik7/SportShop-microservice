package com.ecommerce.order.orderLine;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = {
        OrderLineController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class OrderLineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderLineService orderLineService;

    @Test
    void OrderLineController_GetOrderLinesByIds_Success() throws Exception {

        OrderLineResponse orderLine1 = OrderLineResponse.builder()
                .id(1)
                .productItemId(1)
                .productName("Product1")
                .qty(1)
                .price(10.0)
                .build();

        OrderLineResponse orderLine2 = OrderLineResponse.builder()
                .id(2)
                .productItemId(2)
                .productName("Product2")
                .qty(2)
                .price(11.0)
                .build();

        when(orderLineService.getOrderLinesByIds(List.of(1, 2)))
                .thenReturn(List.of(orderLine1, orderLine2));

        mockMvc.perform(get("/order-line/get-by-ids")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("orderLinesIds", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productName").value("Product1"))
                .andExpect(jsonPath("$[0].price").value(10.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].productName").value("Product2"))
                .andExpect(jsonPath("$[1].price").value(11.0));

        verify(orderLineService).getOrderLinesByIds(List.of(1, 2));
    }

    @Test
    void OrderLineController_GetOrderLineById_Success() throws Exception {
        Jwt jwt = mock(Jwt.class);

        OrderLineResponse orderLine1 = OrderLineResponse.builder()
                .id(1)
                .productItemId(1)
                .productName("Product1")
                .qty(1)
                .price(10.0)
                .build();

        when(orderLineService.getOrderLineById(eq(1), any(Jwt.class)))
                .thenReturn(orderLine1);

        mockMvc.perform(get("/order-line/{orderLineId}", orderLine1.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Product1"))
                .andExpect(jsonPath("$.productItemId").value(1))
                .andExpect(jsonPath("$.qty").value(1))
                .andExpect(jsonPath("$.price").value(10.0));

        verify(orderLineService).getOrderLineById(eq(1), any(Jwt.class));
    }

    @Test
    void OrderLineController_GetOrderLineByProductItemIds_Success() throws Exception {
        OrderLineResponse orderLine1 = OrderLineResponse.builder()
                .id(1)
                .productItemId(1)
                .productName("Product1")
                .qty(1)
                .price(10.0)
                .build();

        OrderLineResponse orderLine2 = OrderLineResponse.builder()
                .id(2)
                .productItemId(2)
                .productName("Product2")
                .qty(2)
                .price(11.0)
                .build();

        when(orderLineService.getOrderLineByProductItemIds(List.of(1, 2)))
                .thenReturn(List.of(orderLine1, orderLine2));

        mockMvc.perform(get("/order-line/by-product-item-ids")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("productItemIds", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productName").value("Product1"))
                .andExpect(jsonPath("$[0].price").value(10.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].productName").value("Product2"))
                .andExpect(jsonPath("$[1].price").value(11.0));

        verify(orderLineService).getOrderLineByProductItemIds(List.of(1, 2));
    }

    @Test
    void OrderLineController_CanUserReviewOrderLine_Success() throws Exception {
        when(orderLineService.canUserReviewOrderLine(eq(1), eq(5), any(Jwt.class)))
                .thenReturn(true);

        mockMvc.perform(get("/order-line/can-review/{orderLineId}", 1)
                        .param("productItemId", "5")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderLineService).canUserReviewOrderLine(eq(1), eq(5), any(Jwt.class));
    }
}
