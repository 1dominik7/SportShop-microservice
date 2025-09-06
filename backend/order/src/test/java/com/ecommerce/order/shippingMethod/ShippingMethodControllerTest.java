package com.ecommerce.order.shippingMethod;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.order.orderStatus.OrderStatusController;
import com.ecommerce.order.orderStatus.OrderStatusService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
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
        ShippingMethodController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class ShippingMethodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShippingMethodService shippingMethodService;

    @Test
    void ShippingMethodController_CreateShippingMethod_Success() throws Exception {

        ShippingMethodRequest shippingMethodRequest = ShippingMethodRequest.builder()
                .name("Test delivery")
                .price(10.0)
                .build();

        ShippingMethod shippingMethod = ShippingMethod.builder()
                .id(1)
                .name("Test delivery")
                .price(10.0)
                .build();

        when(shippingMethodService.addShippingMethod(any(ShippingMethodRequest.class)))
                .thenReturn(shippingMethod);

        mockMvc.perform(post("/shipping-method")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(shippingMethodRequest))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test delivery"))
                .andExpect(jsonPath("$.price").value("10.0"));
    }

    @Test
    void ShippingMethodController_GetAllShippingMethod_Success() throws Exception {

        ShippingMethodResponse shippingMethod1 = ShippingMethodResponse.builder()
                .id(1)
                .name("Test delivery1")
                .price(10.0)
                .build();

        ShippingMethodResponse shippingMethod2 = ShippingMethodResponse.builder()
                .id(2)
                .name("Test delivery2")
                .price(11.0)
                .build();

        when(shippingMethodService.getAllShippingMethod())
                .thenReturn(List.of(shippingMethod1,shippingMethod2));

        mockMvc.perform(get("/shipping-method/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test delivery1"))
                .andExpect(jsonPath("$[0].price").value(10.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Test delivery2"))
                .andExpect(jsonPath("$[1].price").value(11.0));
    }

    @Test
    void ShippingMethodController_UpdateShippingMethod_Success() throws Exception {
        Integer shippingMethodId = 1;
        ShippingMethodRequest shippingMethodRequest = ShippingMethodRequest.builder()
                .name("Test updated")
                .price(12.0)
                .build();

        ShippingMethodResponse updated = ShippingMethodResponse.builder()
                .id(shippingMethodId)
                .name("Test updated")
                .price(12.0)
                .build();

        when(shippingMethodService.updateShippingMethod(any(ShippingMethodRequest.class), eq(shippingMethodId)))
                .thenReturn(updated);

        mockMvc.perform(put("/shipping-method/{shippingMethodId}", shippingMethodId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(shippingMethodRequest))).andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test updated"))
                .andExpect(jsonPath("$.price").value(12));
    }

    @Test
    void ShippingMethodController_DeleteShippingMethod_Success() throws Exception {
        Integer shippingMethodId = 1;

        doNothing().when(shippingMethodService).deleteShippingMethod(shippingMethodId);

        mockMvc.perform(delete("/shipping-method/{shippingMethodId}", shippingMethodId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Shipping method has been successfully deleted!"));
    }
}
