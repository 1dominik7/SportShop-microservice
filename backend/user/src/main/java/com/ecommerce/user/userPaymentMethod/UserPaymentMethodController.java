package com.ecommerce.user.userPaymentMethod;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("user-payment-method")
public class UserPaymentMethodController {

    final private UserPaymentMethodService userPaymentMethodService;

    @PostMapping
    public ResponseEntity<UserPaymentMethod> createUserPaymentMethod(@RequestBody UserPaymentMethodRequest request,
                                                                     @AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        UserPaymentMethod userPaymentMethod = userPaymentMethodService.createUserPaymentMethod(request, currentKeycloakId);
        return ResponseEntity.status(HttpStatus.CREATED).body(userPaymentMethod);
    }

    @GetMapping
    public ResponseEntity<List<UserPaymentMethodResponse>> getUserPaymentMethods(@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        List<UserPaymentMethodResponse> userPaymentMethodResponses = userPaymentMethodService.getUserPaymentMethods(currentKeycloakId);
        return ResponseEntity.ok(userPaymentMethodResponses);
    }

    @GetMapping("/{paymentMethodId}")
    public ResponseEntity<UserPaymentMethodResponse> getUserPaymentMethodById(
            @PathVariable String paymentMethodId
            , @AuthenticationPrincipal Jwt jwt
    ) {
        String currentKeycloakId = jwt.getSubject();
        UserPaymentMethodResponse response = userPaymentMethodService.getUserPaymentMethodById(paymentMethodId, currentKeycloakId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{paymentMethodId}")
    public ResponseEntity<String> deleteUserPaymentMethodById(@PathVariable String paymentMethodId
            , @AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        userPaymentMethodService.deleteUserPaymentMethodById(paymentMethodId, currentKeycloakId);
        return ResponseEntity.ok("User method has been has been successfully deleted");
    }
}
