package com.ecommerce.payment.paymentType;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        PaymentTypeController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class PaymentTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentTypeService paymentTypeService;

    @Test
    void PaymentTypeController_CreatePaymentType_Success() throws Exception {

        PaymentTypeRequest paymentTypeRequest = PaymentTypeRequest.builder()
                .value("Visa")
                .build();

        PaymentTypeResponse paymentType = PaymentTypeResponse.builder()
                .id(1)
                .value("Visa")
                .build();

        when(paymentTypeService.createPaymentType(any(PaymentTypeRequest.class)))
                .thenReturn(paymentType);

        mockMvc.perform(post("/payment-type")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(paymentTypeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.value").value("Visa"));
    }

    @Test
    void PaymentTypeController_GetPaymentTypeResponses_Success() throws Exception {

        PaymentTypeResponse paymentType1 = PaymentTypeResponse.builder()
                .id(1)
                .value("Visa")
                .build();

        PaymentTypeResponse paymentType2 = PaymentTypeResponse.builder()
                .id(2)
                .value("Mastercard")
                .build();

        List<PaymentTypeResponse> paymentTypes = List.of(paymentType1, paymentType2);

        when(paymentTypeService.getAllPaymentTypes()).thenReturn(paymentTypes);

        mockMvc.perform(get("/payment-type/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].value").value("Visa"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].value").value("Mastercard"));
    }



}
