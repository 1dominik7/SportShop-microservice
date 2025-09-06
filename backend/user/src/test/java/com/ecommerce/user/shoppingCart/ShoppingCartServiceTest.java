package com.ecommerce.user.shoppingCart;

import com.ecommerce.user.address.AddressService;
import com.ecommerce.user.clients.ProductCallerService;
import com.ecommerce.user.clients.dto.ProductItemOneByColour;
import com.ecommerce.user.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItem;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scala.Int;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @Mock
    private ProductCallerService productCallerService;

    private Role createRole(String name) {
        return new Role(null, name, null, LocalDateTime.now(), null);
    }

    @Test
    void ShoppingCartService_AddProductToCart_Success() {

        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;
        ShoppingCart cart = new ShoppingCart();

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(productItemId)
                .qtyInStock(10)
                .build();
        ProductItemOneByColourResponse productResponse = new ProductItemOneByColourResponse();
        productResponse.setProductName("Test Product");
        productResponse.setProductItemOneByColour(List.of(productItem));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(productItemId, null)).thenReturn(productResponse);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingCartResponse response = shoppingCartService.addProductToCart(productItemId, quantity, keycloakId);

        assertNotNull(response);
        assertEquals(1, response.getShoppingCartItems().size());
        assertEquals(productItemId, response.getShoppingCartItems().get(0).getProductItemId());
        assertEquals(quantity.intValue(), response.getShoppingCartItems().get(0).getQty());

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(productCallerService).getProductItemById(productItemId, null);
        ;
        verify(userRepository).save(any(User.class));
    }

    @Test
    void ShoppingCartService_AddProductToCart_UpdateExistingItem() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 3;

        ShoppingCartItem existingItem = ShoppingCartItem.builder()
                .productItemId(productItemId)
                .qty(2)
                .build();

        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartItems(new ArrayList<>(List.of(existingItem)));

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(productItemId)
                .qtyInStock(10)
                .build();

        ProductItemOneByColourResponse productResponse = new ProductItemOneByColourResponse();
        productResponse.setProductName("Test Product");
        productResponse.setProductItemOneByColour(List.of(productItem));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(productItemId, null)).thenReturn(productResponse);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingCartResponse response = shoppingCartService.addProductToCart(productItemId, quantity, keycloakId);

        assertNotNull(response);
        assertEquals(1, response.getShoppingCartItems().size());
        assertEquals(productItemId, response.getShoppingCartItems().get(0).getProductItemId());
        assertEquals(5, response.getShoppingCartItems().get(0).getQty());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void ShoppingCartService_AddProductToCart_ReturnsEmptyCart() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 1;
        ShoppingCart cart = new ShoppingCart();

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(productItemId, null)).thenReturn(null);

        ShoppingCartResponse response = shoppingCartService.addProductToCart(productItemId, quantity, keycloakId);

        assertNotNull(response);
        assertTrue(response.getShoppingCartItems().isEmpty());

        verify(userRepository, never()).save(any());
    }

    @Test
    void ShoppingCartService_AddProductToCart_ThrowsException_WhenProductItemIdIsNull() {
        APIException ex = assertThrows(APIException.class, () ->
                shoppingCartService.addProductToCart(null, 1, "keycloak-123"));
        assertEquals("Invalid product or quantity", ex.getMessage());
    }

    @Test
    void ShoppingCartService_AddProductToCart_ThrowsException_WhenQuantityIsInvalid() {
        APIException ex1 = assertThrows(APIException.class, () ->
                shoppingCartService.addProductToCart(1, 0, "keycloak-123"));
        assertEquals("Invalid product or quantity", ex1.getMessage());

        APIException ex2 = assertThrows(APIException.class, () ->
                shoppingCartService.addProductToCart(1, null, "keycloak-123"));
        assertEquals("Invalid product or quantity", ex2.getMessage());
    }

    @Test
    void ShoppingCartService_AddProductToCart_ThrowsException_WhenQuantityExceedsStock() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 15;
        ShoppingCart cart = new ShoppingCart();

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();


        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(productItemId)
                .qtyInStock(10)
                .build();

        ProductItemOneByColourResponse productResponse = new ProductItemOneByColourResponse();
        productResponse.setProductName("Test Product");
        productResponse.setProductItemOneByColour(List.of(productItem));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(productItemId, null)).thenReturn(productResponse);

        APIException ex = assertThrows(APIException.class, () ->
                shoppingCartService.addProductToCart(productItemId, quantity, keycloakId));
        assertEquals("Only 10 units of product Test Product are in stock", ex.getMessage());

    }

    @Test
    void ShoppingCartService_GetUserCartByKeycloak_Success() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;

        ShoppingCartItem existingItem = ShoppingCartItem.builder()
                .productItemId(productItemId)
                .qty(quantity)
                .build();

        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartItems(new ArrayList<>(List.of(existingItem)));

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(productItemId)
                .qtyInStock(10)
                .build();

        ProductItemOneByColourResponse productItemResponse = new ProductItemOneByColourResponse();
        productItemResponse.setProductName("Test Product");
        productItemResponse.setProductItemOneByColour(List.of(productItem));
        productItemResponse.setProductItemId(productItemId);

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemByIds(List.of(productItemId)))
                .thenReturn(List.of(productItemResponse));

        ShoppingCartGetProdItemResponse response = shoppingCartService.getUserCartByKeycloak(keycloakId);

        assertNotNull(response);
        assertEquals(1, response.getShoppingCartItems().size());
        assertEquals(productItemId, response.getShoppingCartItems().get(0).getProductItem().getProductItemId());
        assertEquals(quantity, response.getShoppingCartItems().get(0).getQty());
    }


    @Test
    void ShoppingCartService_UpdateProductQuantityInCart_AddNewItemWhenNotInCart() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;

        ShoppingCartItem existingItem = ShoppingCartItem.builder()
                .productItemId(productItemId)
                .qty(quantity)
                .build();

        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartItems(new ArrayList<>(List.of(existingItem)));

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(productItemId)
                .qtyInStock(10)
                .build();

        ProductItemOneByColourResponse productItemResponse = new ProductItemOneByColourResponse();
        productItemResponse.setProductName("Test Product");
        productItemResponse.setProductItemOneByColour(List.of(productItem));
        productItemResponse.setProductItemId(productItemId);

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(anyInt(), isNull())).thenReturn(productItemResponse);
        when(userRepository.save(any(User.class))).thenReturn(user);

        ShoppingCartResponse result = shoppingCartService.updateProductQuantityInCart(1, 2, keycloakId);

        assertEquals(1, result.getShoppingCartItems().size());
        assertEquals(productItemId, result.getShoppingCartItems().get(0).getProductItemId());
        assertEquals(4, result.getShoppingCartItems().get(0).getQty());
    }

    @Test
    void ShoppingCartService_UpdateProductQuantityInCart_RemoveNonExistingItem() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;

        ShoppingCartItem existingCart = ShoppingCartItem.builder()
                .productItemId(productItemId)
                .qty(quantity)
                .build();

        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartItems(new ArrayList<>(List.of(existingCart)));

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(2)
                .qtyInStock(10)
                .productName("Test Product")
                .build();

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        ProductItemOneByColourResponse productItemResponse = new ProductItemOneByColourResponse();
        productItemResponse.setProductName("Test Product");
        productItemResponse.setProductItemOneByColour(List.of(productItem));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(2, null)).thenReturn(productItemResponse);

        APIException exception = assertThrows(APIException.class, () ->
                shoppingCartService.updateProductQuantityInCart(2, -1, keycloakId));

        assertEquals("Cannot remove product that is not in cart", exception.getMessage());
    }

    @Test
    void ShoppingCartService_UpdateProductQuantityInCart_WhenQuantityWouldBecomeNegative() {
        String keycloakId = "keycloak-123";
        Integer productItemId = 1;
        Integer quantity = 2;

        ShoppingCartItem existingCart = ShoppingCartItem.builder()
                .productItemId(productItemId)
                .qty(quantity)
                .build();

        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartItems(new ArrayList<>(List.of(existingCart)));

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(productItemId)
                .qtyInStock(10)
                .productName("Test Product")
                .build();

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        ProductItemOneByColourResponse productItemResponse = new ProductItemOneByColourResponse();
        productItemResponse.setProductName("Test Product");
        productItemResponse.setProductItemOneByColour(List.of(productItem));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(productItemId, null)).thenReturn(productItemResponse);

        APIException exception = assertThrows(APIException.class, () ->
                shoppingCartService.updateProductQuantityInCart(productItemId, -3, keycloakId));

        assertEquals("Quantity cannot be negative", exception.getMessage());
    }

    @Test
    void ShoppingCartService_DeleteProductFromCart_RemoveOnlySpecifiedProduct() {
        String keycloakId = "keycloak-123";
        Integer productItemIdToKeep = 1;
        Integer productItemIdToRemove = 2;

        ShoppingCartItem itemToKeep  = ShoppingCartItem.builder()
                .productItemId(productItemIdToKeep)
                .qty(1)
                .build();

        ShoppingCartItem itemToRemove = ShoppingCartItem.builder()
                .productItemId(productItemIdToRemove)
                .qty(2)
                .build();

        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartItems(new ArrayList<>(List.of(itemToKeep, itemToRemove)));

        User user = User.builder()
                .id("1")
                .roles(List.of(createRole("USER")))
                .shoppingCart(cart)
                .build();

        ProductItemOneByColour productItem = ProductItemOneByColour.builder()
                .id(productItemIdToRemove)
                .productName("Test Product")
                .productCode("XYZ123")
                .qtyInStock(10)
                .build();


        ProductItemOneByColourResponse productItemResponse = new ProductItemOneByColourResponse();
        productItemResponse.setProductItemOneByColour(List.of(productItem));
        productItemResponse.setProductName(productItem.getProductName());

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(productCallerService.getProductItemById(productItemIdToRemove, null))
                .thenReturn(productItemResponse);

        String result = shoppingCartService.deleteProductFromCart(productItemIdToRemove, keycloakId);

        assertEquals(1, cart.getShoppingCartItems().size());
        assertEquals(productItemIdToKeep, cart.getShoppingCartItems().get(0).getProductItemId());

    }
}
