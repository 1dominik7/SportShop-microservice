package com.ecommerce.user.shoppingCart;

import com.ecommerce.user.auth.AuthenticationService;
import com.ecommerce.user.auth.RegistrationRequest;
import com.ecommerce.user.clients.ProductCallerService;
import com.ecommerce.user.clients.dto.ProductItemOneByColour;
import com.ecommerce.user.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.user.discountCode.DiscountCode;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCarItemGetProdItemResponse;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItem;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItemResponse;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import com.ecommerce.user.user.UserResponse;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@SpringBootTest
@SpringBootTest(classes = {
        ShoppingCartController.class,
})
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = MailSenderAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingCartService shoppingCartService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private ProductCallerService productCallerService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Jwt jwt;

    @Test
    void ShoppingCart_AddProductToCart_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;

        ShoppingCartItemResponse itemResponse = ShoppingCartItemResponse.builder()
                .productItemId(productItemId)
                .qty(quantity)
                .build();

        ShoppingCartResponse shoppingCartResponse = ShoppingCartResponse.builder()
                .shoppingCartItems(List.of(itemResponse))
                .build();

        when(shoppingCartService.addProductToCart(eq(productItemId),
                eq(quantity),
                eq(keycloakId))).thenReturn(shoppingCartResponse);


        mockMvc.perform(post("/cart/products/{productItemId}/quantity/{quantity}", productItemId, quantity)
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shoppingCartItems[0].productItemId").value(productItemId))
                .andExpect(jsonPath("$.shoppingCartItems[0].qty").value(quantity));
    }

    //dokonczyc
    @Test
    void ShoppingCart_UpdateCartProduct_IncreaseQuantity() throws Exception {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        String operation = "increase";
        Integer expectedQty = 3;

        when(shoppingCartService.updateProductQuantityInCart(eq(productItemId), anyInt(), eq(keycloakId)))
                .thenAnswer(inv -> {
                    Integer addedQuantity = inv.getArgument(1);
                    ShoppingCartItemResponse item = ShoppingCartItemResponse.builder()
                            .productItemId(productItemId)
                            .qty(2 + addedQuantity)
                            .build();
                    return ShoppingCartResponse.builder()
                            .shoppingCartItems(List.of(item))
                            .build();
                });

        mockMvc.perform(put("/cart/update/products/{productItemId}/quantity/{operation}", productItemId, operation)
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shoppingCartItems[0].productItemId").value(productItemId))
                .andExpect(jsonPath("$.shoppingCartItems[0].qty").value(expectedQty));
    }

    @Test
    void ShoppingCart_UpdateCartProduct_DecreaseQuantity() throws Exception {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        String operation = "delete";
        Integer expectedQty = 1;

        when(shoppingCartService.updateProductQuantityInCart(eq(productItemId), anyInt(), eq(keycloakId)))
                .thenAnswer(inv -> {
                    Integer decreaseQuantity = inv.getArgument(1);
                    ShoppingCartItemResponse item = ShoppingCartItemResponse.builder()
                            .productItemId(productItemId)
                            .qty(2 + decreaseQuantity)
                            .build();
                    return ShoppingCartResponse.builder()
                            .shoppingCartItems(List.of(item))
                            .build();
                });

        mockMvc.perform(put("/cart/update/products/{productItemId}/quantity/{operation}", productItemId, operation)
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shoppingCartItems[0].productItemId").value(productItemId))
                .andExpect(jsonPath("$.shoppingCartItems[0].qty").value(expectedQty));
    }

    @Test
    void ShoppingCart_GetCartUserCart_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;

        ShoppingCartItemResponse itemResponse = ShoppingCartItemResponse.builder()
                .productItemId(productItemId)
                .qty(quantity)
                .build();

        ShoppingCartResponse shoppingCartResponse = ShoppingCartResponse.builder()
                .shoppingCartItems(List.of(itemResponse))
                .build();

        UserResponse mockResponse = UserResponse.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .roleNames(List.of("USER"))
                .shoppingCart(shoppingCartResponse)
                .build();

        when(authenticationService.register(any(RegistrationRequest.class))).thenReturn(mockResponse);

        ProductItemOneByColourResponse productResponse = ProductItemOneByColourResponse.builder()
                .productItemId(1)
                .productId(1)
                .productName("Product Test")
                .build();

        ShoppingCarItemGetProdItemResponse cartItem = ShoppingCarItemGetProdItemResponse.builder()
                .productItem(productResponse)
                .qty(2)
                .build();

        DiscountCode discountCode = DiscountCode.builder()
                .id("1")
                .discount(10)
                .code("SUMMER")
                .build();

        ShoppingCartGetProdItemResponse cartResponse = ShoppingCartGetProdItemResponse.builder()
                .shoppingCartItems(List.of(cartItem))
                .discountCodes(Set.of(discountCode))
                .build();

        when(shoppingCartService.getUserCartByKeycloak(keycloakId))
                .thenReturn(cartResponse);

        when(productCallerService.getProductItemByIds(List.of(1)))
                .thenReturn(List.of(productResponse));

        mockMvc.perform(get("/cart/users/cart")
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shoppingCartItems[0].productItem.productItemId").value(1))
                .andExpect(jsonPath("$.shoppingCartItems[0].qty").value(2))
                .andExpect(jsonPath("$.discountCodes[0].code").value("SUMMER"));
    }

    @Test
    void ShoppingCart_deleteProductFromCart_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;

        Role role = Role.builder()
                .id("1")
                .name("USER")
                .build();

        ShoppingCartItem itemResponse = ShoppingCartItem.builder()
                .productItemId(productItemId)
                .qty(quantity)
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .shoppingCartItems(List.of(itemResponse))
                .build();

        User mockResponse = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .roles(List.of(role))
                .shoppingCart(shoppingCart)
                .build();

        ProductItemOneByColour productItemOneByColour = ProductItemOneByColour.builder()
                .id(productItemId)
                .productName("Product Test")
                .productCode("test123")
                .build();

        ProductItemOneByColourResponse productResponse = ProductItemOneByColourResponse.builder()
                .productItemId(1)
                .productItemOneByColour(List.of(productItemOneByColour))
                .productId(1)
                .productName("Product Test")
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(mockResponse));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productCallerService.getProductItemById(productItemId, null)).thenReturn(productResponse);

        when(shoppingCartService.deleteProductFromCart(productItemId, keycloakId)).thenReturn(
                "Product " + productResponse.getProductName() + "/" + productItemOneByColour.getProductCode() + " removed from the cart!");

        mockMvc.perform(delete("/cart/delete/product/{productItemId}", productItemId)
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId))))
                .andExpect(status().isOk())
                .andExpect(content().string("Product " + productResponse.getProductName() + "/" + productItemOneByColour.getProductCode() + " removed from the cart!"));
    }

    @Test
    void ShoppingCart_deleteUserCartItem_Success() throws Exception {
        String keycloakId = "keycloak-123";

        when(shoppingCartService.deleteCart(keycloakId))
                .thenReturn("Shopping cart removed successfully");

        mockMvc.perform(delete("/cart")
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId))))
                .andExpect(status().isOk())
                .andExpect(content().string("Shopping cart removed successfully"));
        verify(shoppingCartService).deleteCart(keycloakId);
    }
}
