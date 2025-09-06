package com.ecommerce.user.userReview;

import com.ecommerce.user.clients.dto.OrderLineResponse;
import com.ecommerce.user.clients.dto.ProductImageResponse;
import com.ecommerce.user.clients.dto.ProductItemResponse;
import com.ecommerce.user.clients.dto.ProductItemToOrderResponse;
import com.ecommerce.user.shoppingCart.ShoppingCartController;
import com.ecommerce.user.user.User;
import com.ecommerce.user.userReview.dto.UserReviewRequest;
import com.ecommerce.user.userReview.dto.UserReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        UserReviewController.class,
})
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = MailSenderAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class UserReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserReviewService userReviewService;

    @Test
    void UserReviewController_CreateReview_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        UserReviewResponse response = UserReviewResponse.builder()
                .id("1")
                .orderLineId(1)
                .ratingValue(5)
                .comment("Great product")
                .build();

        when(userReviewService.createReview(any(UserReviewRequest.class), any(Jwt.class)))
                .thenReturn(response);

        mockMvc.perform(post("/review")
                        .with(jwt().jwt(jwt))
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {
                                  "productId": 1,
                                  "orderLineId": 1,
                                  "ratingValue": 5,
                                  "comment": "Great product"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.orderLineId").value(1))
                .andExpect(jsonPath("$.ratingValue").value(5))
                .andExpect(jsonPath("$.comment").value("Great product"));

        verify(userReviewService).createReview(any(UserReviewRequest.class), any(Jwt.class));
    }

    @Test
    void UserReviewController_GetProductsForReview_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        ProductImageResponse productItemResponse = ProductImageResponse.builder()
                .id(1L)
                .imageFilename("")
                .build();

        ProductItemResponse productItemToResponse1 = ProductItemResponse.builder()
                .productCode("test1")
                .id(1)
                .variationOptionIds(List.of())
                .productImages(Set.of(productItemResponse))
                .build();

        ProductItemResponse productItemResponse2 = ProductItemResponse.builder()
                .productCode("test2")
                .id(2)
                .variationOptionIds(List.of())
                .productImages(Set.of(productItemResponse))
                .build();

        when(userReviewService.getProductsForReview(any(Jwt.class)))
                .thenReturn(List.of(productItemToResponse1, productItemResponse2));

        mockMvc.perform(get("/review/products")
                        .with(jwt().jwt(jwt))
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productCode").value("test1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].productCode").value("test2"));

        verify(userReviewService).getProductsForReview(any(Jwt.class));
    }

    @Test
    void UserReviewController_CanUserReviewProduct_Success() throws Exception {

        Map<Integer, Boolean> canReviewMap = Map.of(
                1, true,
                2, false
        );

        when(userReviewService.canUserReviewProducts(any(Jwt.class), List.of(1, 2))).thenReturn(canReviewMap);

        mockMvc.perform(get("/review/products/can-review")
                        .with(jwt().jwt(Jwt.withTokenValue("token")
                                .header("alg", "none")
                                .claim("sub", "keycloak-123")
                                .build()))
                        .param("orderLineIds", "1", "2")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['1']").value(true))
                .andExpect(jsonPath("$.['2']").value(false));

        verify(userReviewService).canUserReviewProducts(any(Jwt.class), eq(List.of(1, 2)));
    }

    @Test
    void UserReviewController_UpdateReview_Success() throws Exception {
        String reviewId = "review1";

        UserReviewResponse updateReview = UserReviewResponse.builder()
                .id(reviewId)
                .orderLineId(1)
                .ratingValue(3)
                .comment("Great product")
                .createdDate(LocalDateTime.of(2025, 8, 13, 0, 0))
                .build();

        when(userReviewService.editUserReview(eq(reviewId), any(UserReviewRequest.class), anyString()))
                .thenReturn(updateReview);

        mockMvc.perform(put("/review/{userReviewId}", reviewId)
                        .with(jwt().jwt(Jwt.withTokenValue("token")
                                .header("alg", "none")
                                .claim("sub", "keycloak-123")
                                .build()))
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {
                                  "orderLineId": 1,
                                  "ratingValue": 3,
                                  "comment": "Great product",
                                  "createdDate": "2025-08-13T00:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.orderLineId").value(1))
                .andExpect(jsonPath("$.ratingValue").value(3))
                .andExpect(jsonPath("$.comment").value("Great product"));

        verify(userReviewService).editUserReview(eq(reviewId), any(UserReviewRequest.class), eq("keycloak-123"));
    }

    @Test
    void UserReviewController_DeleteReview_Success() throws Exception {
        String reviewId = "review1";

        doNothing().when(userReviewService).deleteUserReview(eq(reviewId), eq("keycloak-123"));

        mockMvc.perform(delete("/review/{userReviewId}", reviewId)
                        .with(jwt().jwt(Jwt.withTokenValue("token")
                                .header("alg", "none")
                                .claim("sub", "keycloak-123")
                                .build()))
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(content().string("User review has been successfully deleted!"));

        verify(userReviewService).deleteUserReview(eq(reviewId), eq("keycloak-123"));
    }
    }
