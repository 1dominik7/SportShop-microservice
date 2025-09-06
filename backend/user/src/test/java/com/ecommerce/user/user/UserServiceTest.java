package com.ecommerce.user.user;

import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.role.Role;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @Mock
    private KeyCloakService keyCloakService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private Role createRole(String name) {
        return new Role(null, name, null, LocalDateTime.now(), null);
    }

    @Test
    void UserService_GetUserProfile_Success(){

        String keycloakId = "keycloak-123";
        Role roleUser = createRole("USER");
        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .roles(List.of(roleUser))
                .firstname("John")
                .lastname("Doe")
                .enabled(false)
                .build();

        when(userRepository.findByKeycloakId("keycloak-123")).thenReturn(Optional.of(user));

        UserResponse userResponse = userService.findUserProfile(keycloakId);

        assertNotNull(userResponse);
        assertEquals(user.getId(), userResponse.getId());
        assertEquals(user.getEmail(), userResponse.getEmail());
        assertEquals(user.isEnabled(), userResponse.isEnabled());
        assertEquals(user.getFirstname(), userResponse.getFirstname());
        assertEquals(user.getLastname(), userResponse.getLastname());
        assertNotNull(userResponse.getRoleNames());
        assertTrue(userResponse.getRoleNames().contains("USER"));
    }

    @Test
    void UserService_GetUserProfile_UserNotFound(){
        String keycloakId = "keycloak-123";

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.findUserProfile(keycloakId);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void UserService_UpdateUserProfile_Success(){
        String keycloakId = "keycloak-123";
        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .firstname("John")
                .lastname("Doe")
                .dateOfBirth(LocalDate.of(1999,1,1))
                .build();
        UserUpdateRequest request = new UserUpdateRequest("newFirstName", "newLastName", LocalDate.of(2000,1,1));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUserProfile(keycloakId, request);

        assertNotNull(updatedUser);
        assertEquals("newFirstName", updatedUser.getFirstname());
        assertEquals("newLastName", updatedUser.getLastname());
        assertEquals(LocalDate.of(2000,1,1), updatedUser.getDateOfBirth());

        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void UserService_UpdateUserProfile_UserNotFound(){
        String keycloakId = "keycloak-123";
        UserUpdateRequest request = new UserUpdateRequest("newFirstName", "newLastName", LocalDate.of(2000,1,1));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateUserProfile(keycloakId, request);
        });

        assertEquals("User not found", exception.getMessage());
    }


}
