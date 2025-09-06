package com.ecommerce.user.auth;

import com.ecommerce.user.UserApplication;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.shoppingCart.ShoppingCartController;
import com.ecommerce.user.user.UserResponse;
import com.stripe.service.testhelpers.issuing.AuthorizationService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@EnableAutoConfiguration(exclude = MailSenderAutoConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
//    @WithMockUser(username="dominik",roles = {"USER", "ADMIN"})
    public void AuthController_CreateUser_ReturnCreated() throws Exception {

        UserResponse mockResponse = UserResponse.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .roleNames(List.of("USER"))
                .build();

        when(authenticationService.register(any(RegistrationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "John",
                                    "lastName": "Doe",
                                    "email": "johndoe@example.com",
                                    "password": "Password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.roleNames[0]").value("USER"));

        verify(authenticationService).register(any(RegistrationRequest.class));
    }

    @Test
    public void AuthController_Authenticate_Success() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .roleNames(List.of("USER"))
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .user(userResponse)
                .accessToken("access123")
                .refreshToken("refresh123")
                .expiresIn(3600)
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class),
                any(HttpServletResponse.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "john@example.com",
                                    "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.firstname").value("John"))
                .andExpect(jsonPath("$.user.lastname").value("Doe"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"))
                .andExpect(jsonPath("$.accessToken").value("access123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authenticationService).authenticate(any(AuthenticationRequest.class), any(HttpServletResponse.class));
    }

    @Test
//    @WithMockUser(username = "dominik", roles = {"USER", "ADMIN"})
    void AuthController_RestartPassword_ReturnsOK() throws Exception {
        when(authenticationService.resetPassword(any(ResetPasswordRequest.class), eq("keycloak-123")))
                .thenReturn(ResponseEntity.ok("Password has been reset successfully"));

        mockMvc.perform(post("/auth/reset-password")
                        .with(jwt().jwt(jwt -> {
                            jwt.subject("keycloak-123");
                        }))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "token": "validToken",
                        "newPassword": "newPass123",
                        "confirmPassword": "newPass123"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Password has been reset successfully"));

        verify(authenticationService).resetPassword(any(ResetPasswordRequest.class), eq("keycloak-123"));
    }
}
