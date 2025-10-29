package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.ShopOrderCallerService;
import com.ecommerce.payment.clients.dto.ShopOrderRequest;
import com.ecommerce.payment.clients.dto.ShopOrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

import static org.hamcrest.Matchers.containsString;
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
        PaymentController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private ShopOrderCallerService shopOrderCallerService;

    @Mock
    private PaymentController paymentController;

    @MockitoBean
    private StripePaymentService stripePaymentService;

    @Test
    void PaymentController_CreateStripeCheckoutSession_Success() throws Exception {
        Jwt jwt = mock(Jwt.class);
        ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder()
                .userId("user-123")
                .orderTotal(100.0)
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .orderRequest(shopOrderRequest)
                .successUrl("http://success")
                .cancelUrl("http://cancel")
                .build();

        ShopOrderResponse shopOrderResponse = ShopOrderResponse.builder()
                .id(1)
                .userId("user-123")
                .orderTotal(100.0)
                .build();

        when(shopOrderCallerService.createShopOrder(any(), any()))
                .thenReturn(shopOrderResponse);

        when(stripePaymentService.createCheckoutSession(any()))
                .thenReturn("http://success?session_id={CHECKOUT_SESSION_ID}");

        mockMvc.perform(post("/payment/stripe/checkout")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("http://success?session_id={CHECKOUT_SESSION_ID}"))
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void PaymentController_CreateStripeCheckoutSession_ReturnException() throws Exception {
        Jwt jwt = mock(Jwt.class);
        ShopOrderRequest shopOrderRequest = ShopOrderRequest.builder()
                .userId("user-123")
                .orderTotal(100.0)
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .orderRequest(shopOrderRequest)
                .successUrl("http://success")
                .cancelUrl("http://cancel")
                .build();

        when(shopOrderCallerService.createShopOrder(any(), any()))
                .thenThrow(new RuntimeException("Simulated failure"));

        mockMvc.perform(post("/payment/stripe/checkout")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Simulated failure"));
    }

    @Test
    void PaymentController_HandleStripeWebhook_Success() throws Exception {
        doNothing().when(stripePaymentService)
                .handleWebhookEvent(anyString(), anyString());

        mockMvc.perform(post("/payment/stripe/webhook")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("Stripe-Signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("test-payload"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void PaymentController_HandleStripeWebhook_SignatureInvalid() throws Exception {
        doThrow(new SignatureVerificationException("Invalid signature", null))
                .when(stripePaymentService)
                .handleWebhookEvent(anyString(), anyString());

        mockMvc.perform(post("/payment/stripe/webhook")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("Stripe-Signature", "bad-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("test-payload"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid signature"));
    }

    @Test
    void PaymentController_HandleStripeWebhook_StripeException() throws Exception {
        StripeException stripeException = mock(StripeException.class);
        when(stripeException.getMessage()).thenReturn("Stripe is down");
        doThrow(stripeException)
                .when(stripePaymentService)
                .handleWebhookEvent(anyString(), anyString());

        mockMvc.perform(post("/payment/stripe/webhook")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("Stripe-Signature", "sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("test-payload"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Stripe error: Stripe is down"));
    }

    @Test
    void PaymentController_HandleStripeWebhook_MetadataInvalid() throws Exception {
        doThrow(new NumberFormatException("Invalid ID"))
                .when(stripePaymentService)
                .handleWebhookEvent(anyString(), anyString());

        mockMvc.perform(post("/payment/stripe/webhook")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("Stripe-Signature", "sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("test-payload"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid order_id or user_id format"));
    }

    @Test
    void PaymentController_HandleStripeWebhook_UnexpectedError() throws Exception {
        doThrow(new RuntimeException("Unexpected fail"))
                .when(stripePaymentService)
                .handleWebhookEvent(anyString(), anyString());

        mockMvc.perform(post("/payment/stripe/webhook")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("Stripe-Signature", "sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("test-payload"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal server error"));
    }

    @Test
    void PaymentController_VerifyPayment_Success() throws Exception {
        Jwt jwt = mock(Jwt.class);
        String sessionId = "sess_123";

        PaymentVerificationResponse response = PaymentVerificationResponse.builder()
                .status(Payment.PaymentStatus.SUCCEEDED)
                .paymentIntentId(sessionId)
                .transactionId("trans-123")
                .build();

        when(paymentService.verifyPayment(eq(sessionId), any()))
                .thenReturn(response);

        mockMvc.perform(get("/payment/verify/" + sessionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.paymentIntentId").value(sessionId))
                .andExpect(jsonPath("$.transactionId").value("trans-123"));
    }

    @Test
    void PaymentController_VerifyPayment_PaymentFailed() throws Exception {
        Jwt jwt = mock(Jwt.class);
        String sessionId = "sess_123";

        PaymentVerificationResponse response = PaymentVerificationResponse.builder()
                .status(Payment.PaymentStatus.FAILED)
                .paymentIntentId(sessionId)
                .transactionId("trans-123")
                .build();

        when(paymentService.verifyPayment(eq(sessionId), any()))
                .thenReturn(response);

        mockMvc.perform(get("/payment/verify/" + sessionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.transactionId").value("trans-123"))
                .andExpect(jsonPath("$.paymentIntentId").value(sessionId));
    }

    @Test
    void PaymentController_VerifyPayment_StripeExceptionOccurs() throws Exception {
        Jwt jwt = mock(Jwt.class);
        String sessionId = "sess_123";

        StripeException stripeEx = new InvalidRequestException("Stripe error occurred", null, null, null, 400, null);

        when(paymentService.verifyPayment(eq(sessionId), any()))
                .thenThrow(stripeEx);

        mockMvc.perform(get("/payment/verify/" + sessionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error during verify payment: Stripe error occurred")));
    }

    @Test
    void PaymentController_VerifyPayment_UnexpectedExceptionOccurs() throws Exception {
        Jwt jwt = mock(Jwt.class);
        String sessionId = "sess_123";

        when(paymentService.verifyPayment(eq(sessionId), any()))
                .thenThrow(new RuntimeException("Unknown issue"));

        mockMvc.perform(get("/payment/verify/" + sessionId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("An unexpected error occurred: Unknown issue")));
    }

}

