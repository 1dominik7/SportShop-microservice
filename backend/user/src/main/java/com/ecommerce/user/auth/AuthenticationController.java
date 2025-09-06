package com.ecommerce.user.auth;

import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
@RequestMapping("auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        UserResponse userResponse = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authentication(
            @RequestBody @Valid AuthenticationRequest request,
            HttpServletResponse response
    ) throws MessagingException, AccessDeniedException {
        return ResponseEntity.ok(authenticationService.authenticate(request,response));
    }

    @GetMapping("/activate-account")
    public ResponseEntity<AuthenticationResponse> confirm(
            @RequestParam String token,
            @AuthenticationPrincipal Jwt jwt
    ) throws MessagingException {
        String currentKeycloakId = jwt.getSubject();
        AuthenticationResponse response = authenticationService.activateAccount(token, currentKeycloakId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password" )
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request)
            throws MessagingException {
        return authenticationService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request, @AuthenticationPrincipal Jwt jwt) throws MessagingException {
        String currentKeycloakId = jwt.getSubject();
        return authenticationService.resetPassword(request, currentKeycloakId);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        authenticationService.logout(response, refreshToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request,HttpServletResponse response) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken != null && !refreshToken.isEmpty()) {
            Map<String, Object> tokens = authenticationService.refreshToken(refreshToken);
            authenticationService.setAuthCookies(response, tokens);
            return ResponseEntity.ok(tokens);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/check-auth")
    public ResponseEntity<UserResponse> checkAuth(@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        UserResponse userResponse = authenticationService.checkAuth(currentKeycloakId);

        return ResponseEntity.ok(userResponse);
    }
}
