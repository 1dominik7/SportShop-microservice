package com.ecommerce.user.userReview;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.user.clients.ShopOrderCallerService;
import com.ecommerce.user.clients.dto.*;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import com.ecommerce.user.userReview.dto.UserReviewRequest;
import com.ecommerce.user.userReview.dto.UserReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserReviewServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShopOrderCallerService shopOrderCallerService;

    @Mock
    private UserReviewRepository userReviewRepository;

    @InjectMocks
    private UserReviewService userReviewService;

    @Mock
    private Jwt jwt;

    private Role createRole(String name) {
        return new Role(null, name, null, LocalDateTime.now(), null);
    }

    @Test
    void UserReviewService_CreateReview_Success() {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        UserReviewRequest request = UserReviewRequest.builder()
                .productId(1)
                .orderLineId(1)
                .ratingValue(5)
                .comment("Great product")
                .createdDate(LocalDateTime.of(2025, 8, 13, 0, 0))
                .build();

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();

        OrderLineResponse orderLineResponse = OrderLineResponse.builder()
                .id(1)
                .qty(2)
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(shopOrderCallerService.getOrderLineById(1, jwt))
                .thenReturn(orderLineResponse);
        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), orderLineResponse.getId())).thenReturn(false);
        when(userReviewRepository.save(any(UserReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserReviewResponse response = userReviewService.createReview(request, jwt);

        assertEquals(user.getId(), response.getId());
        assertEquals(5, response.getRatingValue());
        assertEquals("Great product", response.getComment());
        assertEquals(orderLineResponse.getId(), response.getOrderLineId());

        verify(userReviewRepository).save(any(UserReview.class));
    }

    @Test
    void UserReviewService_CreateReview_AlreadyReviewed() {

        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        UserReviewRequest request = UserReviewRequest.builder()
                .productId(1)
                .orderLineId(1)
                .ratingValue(5)
                .comment("Great product")
                .createdDate(LocalDateTime.of(2025, 8, 13, 0, 0))
                .build();

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();

        OrderLineResponse orderLineResponse = OrderLineResponse.builder()
                .id(1)
                .qty(2)
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(shopOrderCallerService.getOrderLineById(1, jwt))
                .thenReturn(orderLineResponse);
        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), orderLineResponse.getId())).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> userReviewService.createReview(request, jwt));

        verify(userReviewRepository, never()).save(any(UserReview.class));
    }

    @Test
    void UserReviewService_GetProductsForReview_Success() {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        ProductItemToOrderResponse productItemToOrderResponse1 = ProductItemToOrderResponse.builder()
                .productName("Test Product1")
                .productCode("test1")
                .productId(1)
                .id(1)
                .variationOptions(List.of())
                .productImages(List.of())
                .build();

        ProductItemToOrderResponse productItemToOrderResponse2 = ProductItemToOrderResponse.builder()
                .productName("Test Product2")
                .productCode("test2")
                .productId(2)
                .id(2)
                .variationOptions(List.of())
                .productImages(List.of())
                .build();

        OrderLineResponseWithProductItem orderLine1 = OrderLineResponseWithProductItem.builder()
                .id(1)
                .productItem(productItemToOrderResponse1)
                .build();

        OrderLineResponseWithProductItem orderLine2 = OrderLineResponseWithProductItem.builder()
                .id(2)
                .productItem(productItemToOrderResponse2)
                .build();

        ShopOrderResponse shopOrderResponse = ShopOrderResponse.builder()
                .id(1)
                .orderLines(List.of(orderLine1, orderLine2))
                .userId("1")
                .build();

        when(shopOrderCallerService.getUserShopOrders(jwt)).thenReturn(List.of(shopOrderResponse));

        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), 1)).thenReturn(false);
        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), 2)).thenReturn(true);

        List<ProductItemResponse> result = userReviewService.getProductsForReview(jwt);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void UserReviewService_CanUserReviewProduct_Success() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer orderLineId = 1;
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        when(shopOrderCallerService.canUserReviewOrderLine(orderLineId, productItemId, jwt)).thenReturn(true);
        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), orderLineId)).thenReturn(false);

        boolean result = userReviewService.canUserReviewProduct(jwt, productItemId, orderLineId);

        assertTrue(result);
    }

    @Test
    void UserReviewService_CanUserReviewProduct_AlreadyReviewed() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer orderLineId = 1;
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        when(shopOrderCallerService.canUserReviewOrderLine(orderLineId, productItemId, jwt)).thenReturn(true);
        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), orderLineId)).thenReturn(true);

        boolean result = userReviewService.canUserReviewProduct(jwt, productItemId, orderLineId);

        assertFalse(result);
    }

    @Test
    void UserReviewService_CanUserReviewProducts_Success() {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        List<Integer> orderLineIds = List.of(1, 2);

        OrderLineResponse ol1 = OrderLineResponse.builder().id(1).productItemId(10).build();
        OrderLineResponse ol2 = OrderLineResponse.builder().id(2).productItemId(20).build();
        when(shopOrderCallerService.getOrderLinesByIds(orderLineIds)).thenReturn(List.of(ol1, ol2));

        when(shopOrderCallerService.canUserReviewOrderLine(1, 10, jwt)).thenReturn(true);
        when(shopOrderCallerService.canUserReviewOrderLine(2, 20, jwt)).thenReturn(false);
        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), 1)).thenReturn(false);
        when(userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), 2)).thenReturn(false);

        Map<Integer, Boolean> result = userReviewService.canUserReviewProducts(jwt, orderLineIds);

        assertEquals(2, result.size());
        assertTrue(result.get(1));
        assertFalse(result.get(2));
    }

    @Test
    void UserReviewService_EditUserReview_Success() throws ApiException {
        String keycloakId = "keycloak-123";
        String reviewId = "review1";

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        UserReview review = UserReview.builder()
                .id(reviewId)
                .userId("1")
                .orderLineId(1)
                .ratingValue(3)
                .comment("Great product")
                .createdDate(LocalDateTime.of(2025, 8, 13, 0, 0))
                .build();
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        UserReviewRequest reviewRequest = UserReviewRequest.builder()
                .orderLineId(1)
                .ratingValue(5)
                .comment("Edited Comment")
                .createdDate(LocalDateTime.of(2025, 8, 13, 0, 0))
                .build();

        UserReview savedReview = UserReview .builder()
                .id(reviewId)
                .orderLineId(1)
                .ratingValue(5)
                .comment("Edited Comment")
                .createdDate(LocalDateTime.of(2025, 8, 13, 0, 0))
                .build();
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(savedReview);

        UserReviewResponse response = userReviewService.editUserReview(reviewId, reviewRequest, keycloakId);

        assertEquals(reviewId, response.getId());
        assertEquals(5, response.getRatingValue());
        assertEquals("1", user.getId());
        assertEquals("Edited Comment", response.getComment());
    }

    @Test
    void UserReviewService_DeleteUserReview_Success() throws ApiException {
        String keycloakId = "keycloak-123";
        String reviewId = "review1";

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .build();
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        UserReview review = UserReview.builder()
                .id(reviewId)
                .userId("1")
                .orderLineId(1)
                .ratingValue(3)
                .comment("Great product")
                .createdDate(LocalDateTime.of(2025, 8, 13, 0, 0))
                .build();
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        userReviewService.deleteUserReview(reviewId, keycloakId);

        verify(userReviewRepository, times(1)).delete(review);
    }
}

