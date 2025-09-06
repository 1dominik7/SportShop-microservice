package com.ecommerce.user.user;

import com.ecommerce.user.auth.AuthenticationService;
import com.ecommerce.user.auth.RegistrationRequest;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.security.SecurityConfig;
import org.jose4j.jwk.Use;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
//@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Mock
    private Jwt jwt;

    @Test
    public void UserController_GetUserProfile_Success() throws Exception {
        String keycloakId = "keycloak-123";

        UserResponse mockResponse = UserResponse.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .roleNames(List.of("USER"))
                .userPaymentMethodResponses(List.of())
                .build();

        when(userService.findUserProfile(keycloakId)).thenReturn(mockResponse);

        mockMvc.perform(get("/users/profile")
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    public void UserController_UpdateProfile_Success() throws Exception {
        String keycloakId = "keycloak-123";

        User updatedUser = User.builder()
                .id("123")
                .firstname("John1")
                .lastname("Doe1")
                .email("john@example.com")
                .build();

        when(userService.updateUserProfile(eq(keycloakId), any(UserUpdateRequest.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/users/profile")
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId)))
                        .contentType("application/json")
                        .content("""
                                    {
                                        "firstname": "John",
                                        "lastname": "Updated"
                                    }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("John1"))
                .andExpect(jsonPath("$.lastname").value("Doe1"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
        verify(userService, times(1))
                .updateUserProfile(eq(keycloakId), any(UserUpdateRequest.class));
    }
}
