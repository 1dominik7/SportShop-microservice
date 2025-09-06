package com.ecommerce.user.shoppingCart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PostMapping("/products/{productItemId}/quantity/{quantity}")
    public ResponseEntity<ShoppingCartResponse> addProductToCart(@PathVariable Integer productItemId, @PathVariable Integer quantity, @AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        ShoppingCartResponse shoppingCart = shoppingCartService.addProductToCart(productItemId, quantity,currentKeycloakId);
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingCart);
    }

    @GetMapping("/users/cart")
    public ResponseEntity<ShoppingCartGetProdItemResponse> getCartUserCart(@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        ShoppingCartGetProdItemResponse shoppingCartResponse = shoppingCartService.getUserCartByKeycloak(currentKeycloakId);
        return ResponseEntity.status(HttpStatus.OK).body(shoppingCartResponse);
    }

    @PutMapping("/update/products/{productItemId}/quantity/{operation}")
    public ResponseEntity<ShoppingCartResponse> updateCartProduct(@PathVariable Integer productItemId, @PathVariable String operation,@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        ShoppingCartResponse shoppingCartResponse = shoppingCartService.updateProductQuantityInCart(productItemId, operation.equalsIgnoreCase("delete") ? -1 : 1,currentKeycloakId);
        return ResponseEntity.status(HttpStatus.OK).body(shoppingCartResponse);
    }

    @DeleteMapping("/delete/product/{productItemId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Integer productItemId,@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        String status = shoppingCartService.deleteProductFromCart(productItemId,currentKeycloakId);

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    @PostMapping("/add-discount")
    public ResponseEntity<String> addDiscountToCart(@RequestParam String discountCode,@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        shoppingCartService.addDiscountToCart(discountCode,currentKeycloakId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUserCartItem(@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        String status = shoppingCartService.deleteCart(currentKeycloakId);
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}
