package com.ecommerce.user.auth;

import com.ecommerce.user.address.AddressResponse;
import com.ecommerce.user.email.EmailService;
import com.ecommerce.user.email.EmailTemplateName;
import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.role.RoleRepository;

import com.ecommerce.user.shoppingCart.ShoppingCart;
import com.ecommerce.user.shoppingCart.ShoppingCartResponse;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItemResponse;
import com.ecommerce.user.user.*;
import com.ecommerce.user.userPaymentMethod.UserPaymentMethodResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;

import java.nio.file.AccessDeniedException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private final KeyCloakService keyCloakService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    @Value("${application.mailing.frontend.restart-password-url}")
    private String resetPasswordUrl;

    @Transactional
    public UserResponse register(RegistrationRequest request) throws MessagingException {

        boolean existingEmail = userRepository.existsByEmail(request.getEmail());
        if (existingEmail) {
            throw new RuntimeException("This email is already taken!");
        }

        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initialized"));

        String token = keyCloakService.getClientAccessToken();
        String keycloakUserId = keyCloakService.createUser(token, request);

        keyCloakService.assignRealmRoleToUser(token, keycloakUserId);

        ShoppingCart emptyCart = ShoppingCart.builder()
                .shoppingCartItems(new ArrayList<>())
                .discountCodes(new HashSet<>())
                .build();

        var user = User.builder()
                .keycloakId(keycloakUserId)
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userPaymentMethods(new ArrayList<>())
                .addresses(new ArrayList<>())
                .shoppingCart(emptyCart)
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);

        UserResponse userResponse = mapToUserResponse(user);

        return userResponse;
    }

    private void sendValidationEmail(User user) throws MessagingException {
        removeAllPreviousTokens(user);
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }

    private void removeAllPreviousTokens(User user) {
        user.getTokens().clear();
        userRepository.save(user);
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Token
                .builder()
                .id(UUID.randomUUID().toString())
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        user.getTokens().add(token);
        userRepository.save(user);

        return generatedToken;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public LoginResponse authenticate(AuthenticationRequest request, HttpServletResponse response) throws MessagingException, AccessDeniedException {

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new NotFoundException("User", Optional.of(request.getEmail())));

        Map<String, Object> tokens = keyCloakService.loginUser(request.getEmail(), request.getPassword());

        setAuthCookies(response, tokens);

        if (!user.isEnabled()) {
            sendValidationEmail(user);
            throw new APIException("Account is disabled. A new activation email has been sent.");
        }

        boolean hasUserRole = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("USER") || role.getName().equals("ADMIN"));

        if (!hasUserRole) {
            throw new AccessDeniedException("You do not have permission to log in.");
        }

        UserResponse userResponse = mapToUserResponse(user);

        return LoginResponse.builder().
                user(userResponse)
                .accessToken((String) tokens.get("access_token"))
                .refreshToken((String) tokens.get("refresh_token"))
                .expiresIn((Integer) tokens.get("expires_in"))
                .build();
    }


    public void setAuthCookies(HttpServletResponse response, Map<String, Object> tokens) {
        int maxAge = (int) tokens.get("expires_in");

        ResponseCookie accessCookie = ResponseCookie.from("access_token", (String) tokens.get("access_token"))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", (String) tokens.get("refresh_token"))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7 dni
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    @Transactional
    public AuthenticationResponse activateAccount(String token, String currentKeycloakId) throws MessagingException {

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        Token activationToken = user.getTokens().stream()
                .filter(t -> t.getToken().equals(token))
                .findFirst()
                .orElseThrow(() -> new APIException("Invalid token"));

        if (LocalDateTime.now().isAfter(activationToken.getExpiresAt())) {
            user.getTokens().remove(activationToken);
            userRepository.save(user);
            sendValidationEmail(user);
            throw new APIException("Activation token has expired. A new token has been sent to the same email address.");
        }

        if (activationToken.isUsed()) {
            throw new APIException("Token has already been used");
        }

        user.setEnabled(true);
        activationToken.setUsed(true);
        activationToken.setValidatedAt(LocalDateTime.now());
        userRepository.save(user);
        UserResponse userResponse = mapToUserResponse(user);

        return AuthenticationResponse.builder().user(userResponse).build();
    }

    @Transactional
    public ResponseEntity<Map<String, String>> forgotPassword(ForgotPasswordRequest request) throws MessagingException {

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                new NotFoundException("User", Optional.ofNullable(request.getEmail())));

        if (user.isAccountLocked()) {
            throw new APIException("Account is locked");
        }
        if (!user.isEnabled()) {
            throw new APIException("Account is not active");
        }

        String token = generateAndSaveResetToken(user);
        sendResetPasswordEmail(user, token);

        return ResponseEntity.ok(Map.of(
                "message", "Reset password link has been sent to your email",
                "token", token
        ));
    }

    private String generateAndSaveResetToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                .id(UUID.randomUUID().toString())
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        user.getTokens().removeIf(t -> t.getValidatedAt() == null && t.getExpiresAt().isAfter(LocalDateTime.now()));

        user.getTokens().add(token);
        userRepository.save(user);

        return generatedToken;
    }

    private void sendResetPasswordEmail(User user, String token) throws MessagingException {

        String urlWithToken = resetPasswordUrl + "/" + token;

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.RESET_PASSWORD,
                urlWithToken,
                token,
                "Reset your password"
        );
    }

    private void sendPasswordChangedConfirmation(User user) throws MessagingException {

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.PASSWORD_CHANGED,
                null,
                null,
                "Your Password Has Been Changed"
        );
    }

    @Transactional
    public ResponseEntity<String> resetPassword(ResetPasswordRequest request, String currentKeycloakId) throws MessagingException {

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        Token token = user.getTokens().stream()
                .filter(t -> t.getToken().equals(request.getToken()))
                .findFirst()
                .orElseThrow(() -> new APIException("Invalid token"));

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new APIException("Token has expired");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new APIException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        token.setValidatedAt(LocalDateTime.now());

        userRepository.save(user);

        sendPasswordChangedConfirmation(user);

        return ResponseEntity.ok("Password has been reset successfully");
    }

    public void logout(HttpServletResponse response, @CookieValue(value = "refresh_token", required = false) String refreshToken) {

        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");

        if (refreshToken != null) {
            keyCloakService.logoutUser(refreshToken);
        }
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    UserResponse checkAuth(String currentKeycloakId){
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        return mapToUserResponse(user);
    }


private UserResponse mapToUserResponse(User user) {
    return UserResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .firstname(user.getFirstname())
            .lastname(user.getLastname())
            .email(user.getEmail())
            .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
            .createdDate(user.getCreatedDate())
            .dateOfBirth(user.getDateOfBirth())
            .accountLocked(user.isAccountLocked())
            .enabled(user.isEnabled())
            .addresses(user.getAddresses() != null ?
                    user.getAddresses().stream().map(address ->
                            AddressResponse.builder()
                                    .id(address.getId())
                                    .country(address.getCountry())
                                    .city(address.getCity())
                                    .firstName(address.getFirstName())
                                    .lastName(address.getLastName())
                                    .postalCode(address.getPostalCode())
                                    .street(address.getStreet())
                                    .phoneNumber(address.getPhoneNumber())
                                    .addressLine1(address.getAddressLine1())
                                    .addressLine2(address.getAddressLine2())
                                    .build()
                    ).collect(Collectors.toList()) :
                    Collections.emptyList())
            .shoppingCart(ShoppingCartResponse.builder()
                    .shoppingCartItems(Optional.ofNullable(user.getShoppingCart())
                            .map(ShoppingCart::getShoppingCartItems)
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(item -> ShoppingCartItemResponse.builder()
                                    .productItemId(item.getProductItemId())
                                    .qty(item.getQty())
                                    .build())
                            .collect(Collectors.toList()))
                    .build())
            .userPaymentMethodResponses(user.getUserPaymentMethods().stream()
                    .map(paymentMethod -> UserPaymentMethodResponse.builder()
                            .id(paymentMethod.getId())
                            .paymentTypeName(paymentMethod.getPaymentType().getValue())
                            .provider(paymentMethod.getProvider())
                            .last4CardNumber(paymentMethod.getLast4CardNumber())
                            .paymentDate(paymentMethod.getPaymentDate())
                            .expiryDate(paymentMethod.getExpiryDate())
                            .isDefault(paymentMethod.isDefault())
                            .active(paymentMethod.isActive())
                            .build())
                    .collect(Collectors.toList()))
            .build();
}

public Map<String, Object> refreshToken(String refreshToken) {
    return keyCloakService.refreshToken(refreshToken);
}
}
