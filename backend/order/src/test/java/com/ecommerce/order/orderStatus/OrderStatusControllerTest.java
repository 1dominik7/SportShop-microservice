package com.ecommerce.order.orderStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;
import java.util.Optional;

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
        OrderStatusController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class OrderStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderStatusService orderStatusService;

    @Test
    void OrderStatusController_CreateOrderStatus_Success() throws Exception {
        OrderStatusRequest orderStatusRequest = OrderStatusRequest.builder()
                .status("In Order")
                .build();

        OrderStatusResponse orderStatusResponse = OrderStatusResponse.builder()
                .id(1)
                .status("In Order")
                .build();

        when(orderStatusService.createOrderStatus(any(OrderStatusRequest.class))).thenReturn(orderStatusResponse);

        mockMvc.perform(post("/order-status/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(orderStatusRequest))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("In Order"));
    }

    @Test
    void OrderStatusController_GetAllOrderStatuses_Success() throws Exception {
        OrderStatusResponse orderStatusResponse1 = OrderStatusResponse.builder()
                .id(1)
                .status("In Order")
                .build();

        OrderStatusResponse orderStatusResponse2 = OrderStatusResponse.builder()
                .id(2)
                .status("In Delivery")
                .build();


        when(orderStatusService.getAllOrderStatuses()).thenReturn(List.of(orderStatusResponse1, orderStatusResponse2));

        mockMvc.perform(get("/order-status/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("In Order"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("In Delivery"));
    }

    @Test
    void OrderStatusController_DeleteOrderStatus_Success() throws Exception {
        Integer orderStatusId = 1;
        doNothing().when(orderStatusService).deleteOrderStatus(orderStatusId);

        mockMvc.perform(delete("/order-status/{orderStatusId}", orderStatusId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Order status has been successfully deleted!"));
    }

}

