package com.ecommerce.user.auth;

import com.ecommerce.user.clients.ShopOrderCallerService;
import com.ecommerce.user.email.EmailService;
import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.role.RoleRepository;
import com.ecommerce.user.user.Token;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import com.ecommerce.user.user.UserResponse;
import com.ecommerce.user.userReview.UserReviewRepository;
import com.ecommerce.user.userReview.UserReviewService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.jose4j.jwk.Use;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private ShopOrderCallerService shopOrderCallerService;

    @Mock
    private UserReviewRepository userReviewRepository;

    @Mock
    private KeyCloakService keyCloakService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserReviewService userReviewService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private Jwt jwt;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private Role createRole(String name) {
        return new Role(null, name, null, LocalDateTime.now(), null);
    }

    @Test
    public void AuthService_CreateUser_Success() throws Exception {

        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("Password123")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(keyCloakService.getClientAccessToken()).thenReturn("mock-token");
        when(keyCloakService.createUser(anyString(), eq(request))).thenReturn("keycloak-id");

        when(roleRepository.findByName("USER")).thenReturn(java.util.Optional.of(createRole("USER")));

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        UserResponse userResponse = authenticationService.register(request);

        assertNotNull(userResponse);
        assertEquals("John", userResponse.getFirstname());
        assertEquals("Doe", userResponse.getLastname());
        assertEquals("johndoe@example.com", userResponse.getEmail());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(keyCloakService).createUser(anyString(), eq(request));
        verify(roleRepository).findByName("USER");
        verify(passwordEncoder).encode("Password123");
    }

    @Test
    public void AuthService_EmailAlreadyExists_ThrowsException() {
        RegistrationRequest request = RegistrationRequest.builder()
                .email("john@example.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.register(request);
        });
        assertEquals("This email is already taken!", exception.getMessage());

        verify(userRepository).existsByEmail(request.getEmail());
        verifyNoMoreInteractions(userRepository, keyCloakService, roleRepository, passwordEncoder);
    }

    @Test
    public void AuthService_NoUserRole_ThrowsException() {
        RegistrationRequest request = RegistrationRequest.builder()
                .email("john@example.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(java.util.Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authenticationService.register(request);
        });

        assertEquals("ROLE USER was not initialized", exception.getMessage());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(roleRepository).findByName("USER");
        verifyNoMoreInteractions(keyCloakService, passwordEncoder);
    }

    @Test
    public void AuthService_SignIn_Success() throws AccessDeniedException, MessagingException {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john@example.com")
                .password("Password123")
                .build();

        User mockUser = User.builder()
                .email("john@example.com")
                .password("encoded-password")
                .enabled(true)
                .roles(List.of(createRole("USER")))
                .userPaymentMethods(List.of())
                .build();

        Map<String, Object> mockTokens = Map.of(
                "access_token", "access123",
                "refresh_token", "refresh123",
                "expires_in", 3600
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(keyCloakService.loginUser("john@example.com", "Password123")).thenReturn(mockTokens);

        LoginResponse result = authenticationService.authenticate(request, mock(HttpServletResponse.class));

        assertNotNull(result);
        assertEquals("access123", result.getAccessToken());
        assertEquals("refresh123", result.getRefreshToken());
        assertEquals(3600, result.getExpiresIn());

        verify(userRepository).findByEmail("john@example.com");
        verify(keyCloakService).loginUser("john@example.com", "Password123");
    }

    @Test
    void AuthService_Authentication_UserNotFound() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john@email.com")
                .password("Password123")
                .build();

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                authenticationService.authenticate(request, mock(HttpServletResponse.class)));

        verify(userRepository).findByEmail("john@email.com");
        verifyNoMoreInteractions(keyCloakService);
    }

    @Test
    void AuthService_Authentication_DisabledAccount() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john@example.com")
                .password("Password123")
                .build();

        Role roleUser = createRole("USER");
        User mockUser = User.builder()
                .email("john@example.com")
                .password("encoded-password")
                .enabled(false)
                .roles(List.of(roleUser))
                .build();

        Map<String, Object> mockTokens = Map.of(
                "access_token", "access123",
                "refresh_token", "refresh123",
                "expires_in", 3600
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(keyCloakService.loginUser(anyString(), anyString())).thenReturn(mockTokens);
        doNothing().when(emailService).sendEmail(any(), any(), any(), any(), any(), any());

        assertThrows(APIException.class, () ->
                authenticationService.authenticate(request, mock(HttpServletResponse.class)));

        verify(userRepository).findByEmail("john@example.com");
        verify(emailService).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void AuthService_Authentication_NoPermission() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john@example.com")
                .password("Password123")
                .build();

        User mockUser = User.builder()
                .email("john@example.com")
                .enabled(true)
                .roles(List.of(createRole("OTHER")))
                .build();


        Map<String, Object> mockTokens = Map.of(
                "access_token", "access123",
                "refresh_token", "refresh123",
                "expires_in", 3600
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(keyCloakService.loginUser(anyString(), anyString())).thenReturn(mockTokens);

        assertThrows(AccessDeniedException.class, () ->
                authenticationService.authenticate(request, mock(HttpServletResponse.class)));

        verify(userRepository).findByEmail("john@example.com");
    }

    private Token createToken(String tokenValue, boolean used, LocalDateTime expireAt) {
        return Token.builder()
                .id(UUID.randomUUID().toString())
                .token(tokenValue)
                .createdAt(LocalDateTime.now())
                .expiresAt(expireAt)
                .used(used)
                .build();
    }

    private User createUserWithTokens(String keycloakId, List<Token> tokens, boolean enabled) {
        return User.builder()
                .keycloakId(keycloakId)
                .email("test@example.com")
                .userPaymentMethods(List.of())
                .enabled(enabled)
                .tokens(new ArrayList<>(tokens))
                .roles(List.of(createRole("USER")))
                .build();
    }

    @Test
    void AuthService_ActiveAccount_Success() throws MessagingException {
        String tokenValue = "validToken123";
        Token token = createToken(tokenValue, false, LocalDateTime.now().plusMinutes(10));

        User user = createUserWithTokens("keycloak-123", List.of(token), false);

        when(userRepository.findByKeycloakId("keycloak-123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        AuthenticationResponse response = authenticationService.activateAccount(tokenValue, "keycloak-123");

        assertTrue(response.getUser().isEnabled());
        assertTrue(user.isEnabled());
        assertTrue(token.isUsed());
        assertNotNull(token.getValidatedAt());
        assertTrue(token.getValidatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));

        verify(userRepository).save(user);
    }

    @Test
    void AuthService_ActiveAccount_InvalidToken() {
        String searchedToken = "validToken123";
        Token token = createToken("token123", false, LocalDateTime.now().plusMinutes(10));
        User user = createUserWithTokens("keycloak-123", List.of(token), false);

        when(userRepository.findByKeycloakId("keycloak-123")).thenReturn(Optional.of(user));
        APIException exception = assertThrows(APIException.class, () ->
                authenticationService.activateAccount(searchedToken, "keycloak-123"));
        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void AuthService_ActiveAccount_ExpiredToken() throws MessagingException {
        String tokenValue = "validToken123";
        Token token = createToken(tokenValue, false, LocalDateTime.now().minusSeconds(10));
        User user = createUserWithTokens("keycloak-123", List.of(token), false);

        when(userRepository.findByKeycloakId("keycloak-123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendEmail(any(), any(), any(), any(), any(), any());
        APIException exception = assertThrows(APIException.class, () ->
                authenticationService.activateAccount(tokenValue, "keycloak-123"));
        assertEquals("Activation token has expired. A new token has been sent to the same email address.", exception.getMessage());
        assertFalse(user.getTokens().contains(token));
        verify(userRepository, times(3)).save(user);
        verify(emailService).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void AuthService_ActiveAccount_TokenUsed() throws MessagingException {
        String tokenValue = "validToken123";
        Token token = createToken(tokenValue, true, LocalDateTime.now().plusMinutes(10));
        User user = createUserWithTokens("keycloak-123", List.of(token), false);

        when(userRepository.findByKeycloakId("keycloak-123")).thenReturn(Optional.of(user));
        APIException exception = assertThrows(APIException.class, () ->
                authenticationService.activateAccount(tokenValue, "keycloak-123"));
        assertEquals("Token has already been used", exception.getMessage());
    }

    @Test
    void AuthService_RestartPassword_Success() throws MessagingException {
        String currentKeycloakId = "keycloak-123";
        String tokenValue = "validToken123";

        Token token = createToken(tokenValue, false, LocalDateTime.now().plusMinutes(10));
        User user = createUserWithTokens(currentKeycloakId, List.of(token), false);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(tokenValue)
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        when(userRepository.findByKeycloakId(currentKeycloakId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendEmail(any(), any(), any(), any(), any(), any());

        ResponseEntity<String> response = authenticationService.resetPassword(request, currentKeycloakId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password has been reset successfully", response.getBody());
        assertEquals("encodedNewPassword", user.getPassword());
        assertNotNull(token.getValidatedAt());

        verify(userRepository).save(user);
        verify(emailService).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void AuthService_RestartPassword_InvalidToken() {
        String currentKeycloakId = "keycloak-123";
        User user = createUserWithTokens(currentKeycloakId, Collections.emptyList(), false);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("invalidToken")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        when(userRepository.findByKeycloakId(currentKeycloakId)).thenReturn(Optional.of(user));

        APIException ex = assertThrows(APIException.class, () ->
                authenticationService.resetPassword(request, currentKeycloakId));
        assertEquals("Invalid token", ex.getMessage());
    }

    @Test
    void AuthService_RestartPassword_ExpiredToken(){
        String currentKeycloakId = "keycloak-123";
        Token expiredToken = createToken("expiredToken", false, LocalDateTime.now().minusMinutes(5));
        User user = createUserWithTokens(currentKeycloakId, List.of(expiredToken), false);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("expiredToken")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        when(userRepository.findByKeycloakId(currentKeycloakId)).thenReturn(Optional.of(user));

        APIException ex = assertThrows(APIException.class, () ->
                authenticationService.resetPassword(request, currentKeycloakId));
        assertEquals("Token has expired", ex.getMessage());
    }

    @Test
    void AuthService_RestartPassword_PasswordDoNotMatch(){
        String currentKeycloakId = "keycloak-123";
        Token token = createToken("validToken", false, LocalDateTime.now().plusMinutes(10));
        User user = createUserWithTokens(currentKeycloakId, List.of(token), false);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("validToken")
                .newPassword("newPassword123")
                .confirmPassword("newPassword1234")
                .build();

        when(userRepository.findByKeycloakId(currentKeycloakId)).thenReturn(Optional.of(user));

        APIException ex =assertThrows(APIException.class, () ->
                authenticationService.resetPassword(request, currentKeycloakId));
        assertEquals("Passwords do not match", ex.getMessage());
    }
}
