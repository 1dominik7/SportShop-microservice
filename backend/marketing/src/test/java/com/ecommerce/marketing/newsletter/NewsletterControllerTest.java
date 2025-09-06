package com.ecommerce.marketing.newsletter;

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

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        NewsletterController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class NewsletterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsletterService newsletterService;

    @Test
    void NewsletterController_AddUserToNewsletter_Success() throws Exception {

        NewsletterRequest request = NewsletterRequest.builder()
                .email("test@gmail.com")
                .build();

        NewsletterResponse newsletter = NewsletterResponse.builder()
                .id(1)
                .email(request.getEmail())
                .couponId("coupon-1")
                .subscribedAt(LocalDateTime.now().toString())
                .build();

        when(newsletterService.addUserToNewsletter(any())).thenReturn(newsletter);


        mockMvc.perform(post("/newsletter/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.couponId").value("coupon-1"));

    }

}
