package com.ecommerce.user.address;

import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import com.ecommerce.user.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private Role createRole(String name) {
        return new Role(null, name, null, LocalDateTime.now(), null);
    }

    @Test
    void AddressService_CreateAddress_Success() {

        String keycloakId = "keycloak-123";
        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .firstname("John")
                .roles(List.of(createRole("USER")))
                .lastname("Doe")
                .enabled(false)
                .addresses(new ArrayList<>())
                .build();

        AddressRequest addressRequest = AddressRequest.builder()
                .id("1")
                .country("Poland")
                .city("Warsaw")
                .firstName("John")
                .lastName("Doe")
                .postalCode("01-001")
                .street("Street")
                .phoneNumber("123123123")
                .addressLine1("63")
                .addressLine2("2/3")
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        AddressResponse response = addressService.createAddress(addressRequest, keycloakId);

        assertNotNull(response);
        assertEquals(addressRequest.getCountry(), response.getCountry());
        assertEquals(addressRequest.getCity(), response.getCity());
        assertEquals(addressRequest.getFirstName(), response.getFirstName());
        assertEquals(addressRequest.getLastName(), response.getLastName());
        assertEquals(addressRequest.getPostalCode(), response.getPostalCode());
        assertEquals(addressRequest.getStreet(), response.getStreet());
        assertEquals(addressRequest.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(addressRequest.getAddressLine1(), response.getAddressLine1());
        assertEquals(addressRequest.getAddressLine2(), response.getAddressLine2());

        assertEquals(1, user.getAddresses().size());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void AddressService_CreateAddress_UserNotFound() {
        String keycloakId = "keycloak-123";
        AddressRequest addressRequest = AddressRequest.builder().build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            addressService.createAddress(addressRequest, keycloakId);
        });

        assertTrue(exception.getMessage().contains("User"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void AddressService_GetUserAddresses_Success() {
        String keycloakId = "keycloak-123";

        Address address = Address.builder()
                .id("1")
                .country("Poland")
                .city("Warsaw")
                .firstName("John")
                .lastName("Doe")
                .postalCode("01-001")
                .street("Street")
                .phoneNumber("123123123")
                .addressLine1("63")
                .addressLine2("2/3")
                .build();

        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .firstname("John")
                .roles(List.of(createRole("USER")))
                .lastname("Doe")
                .enabled(false)
                .addresses((new ArrayList<>(List.of(address))))
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        List<AddressResponse> addressResponses = addressService.getUserAddresses(keycloakId);

        assertNotNull(addressResponses);
        assertEquals(1, addressResponses.size());

        AddressResponse response = addressResponses.get(0);
        assertEquals(address.getId(), response.getId());
        assertEquals(address.getCountry(), response.getCountry());
        assertEquals(address.getCity(), response.getCity());
        assertEquals(address.getFirstName(), response.getFirstName());
        assertEquals(address.getLastName(), response.getLastName());
        assertEquals(address.getPostalCode(), response.getPostalCode());
        assertEquals(address.getStreet(), response.getStreet());
        assertEquals(address.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(address.getAddressLine1(), response.getAddressLine1());
        assertEquals(address.getAddressLine2(), response.getAddressLine2());

        verify(userRepository, times(1)).findByKeycloakId(keycloakId);
    }

    @Test
    void AddressService_UpdateAddress_Success() {
        String keycloakId = "keycloak-123";
        String addressId = "1";

        Address address = Address.builder()
                .id("1")
                .country("Poland")
                .city("Warsaw")
                .firstName("John")
                .lastName("Doe")
                .postalCode("01-001")
                .street("Street")
                .phoneNumber("123123123")
                .addressLine1("63")
                .addressLine2("2/3")
                .build();

        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .firstname("John")
                .roles(List.of(createRole("USER")))
                .lastname("Doe")
                .enabled(false)
                .addresses((new ArrayList<>(List.of(address))))
                .build();

        AddressRequest addressRequest = AddressRequest.builder()
                .id("1")
                .country("PolandUpdate")
                .city("WarsawUpdate")
                .firstName("JohnUpdate")
                .lastName("DoeUpdate")
                .postalCode("02-002")
                .street("StreetUpdate")
                .phoneNumber("333333333")
                .addressLine1("36")
                .addressLine2("3/2")
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse updatedAddressResponse = addressService.updateAddress(addressId, addressRequest, keycloakId);

        assertNotNull(updatedAddressResponse);

        assertEquals(addressRequest.getId(), updatedAddressResponse.getId());
        assertEquals(addressRequest.getCountry(), updatedAddressResponse.getCountry());
        assertEquals(addressRequest.getCity(), updatedAddressResponse.getCity());
        assertEquals(addressRequest.getFirstName(), updatedAddressResponse.getFirstName());
        assertEquals(addressRequest.getLastName(), updatedAddressResponse.getLastName());
        assertEquals(addressRequest.getPostalCode(), updatedAddressResponse.getPostalCode());
        assertEquals(addressRequest.getStreet(), updatedAddressResponse.getStreet());
        assertEquals(addressRequest.getPhoneNumber(), updatedAddressResponse.getPhoneNumber());
        assertEquals(addressRequest.getAddressLine1(), updatedAddressResponse.getAddressLine1());
        assertEquals(addressRequest.getAddressLine2(), updatedAddressResponse.getAddressLine2());

        verify(userRepository, times(1)).findByKeycloakId(keycloakId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void AddressService_UpdateAddress_UserHasNoAddresses() {
        String keycloakId = "keycloak-123";
        String addressId = "1";


        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .firstname("John")
                .roles(List.of(createRole("USER")))
                .lastname("Doe")
                .enabled(false)
                .addresses(null)
                .build();

        AddressRequest addressRequest = AddressRequest.builder()
                .id("1")
                .country("PolandUpdate")
                .city("WarsawUpdate")
                .firstName("JohnUpdate")
                .lastName("DoeUpdate")
                .postalCode("02-002")
                .street("StreetUpdate")
                .phoneNumber("333333333")
                .addressLine1("36")
                .addressLine2("3/2")
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        APIException exception = assertThrows(APIException.class, () -> {
            addressService.updateAddress(addressId, addressRequest, keycloakId);
        });

        assertEquals("User has no addresses", exception.getMessage());

        verify(userRepository, times(1)).findByKeycloakId(keycloakId);
    }

    @Test
    void AddressService_DeleteAddress_Success() {
        String keycloakId = "keycloak-123";
        String addressId = "1";

        Address address = Address.builder()
                .id("1")
                .country("Poland")
                .city("Warsaw")
                .firstName("John")
                .lastName("Doe")
                .postalCode("01-001")
                .street("Street")
                .phoneNumber("123123123")
                .addressLine1("63")
                .addressLine2("2/3")
                .build();

        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .firstname("John")
                .roles(List.of(createRole("USER")))
                .lastname("Doe")
                .enabled(false)
                .addresses(new ArrayList<>(List.of(address)))
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        addressService.deleteAddress(addressId, keycloakId);

        assertEquals(0, user.getAddresses().size());

        verify(userRepository, times(1)).findByKeycloakId(keycloakId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void AddressService_DeleteAddress_NotFoundException() {
        String keycloakId = "keycloak-123";
        String addressId = "1";

        User user = User.builder()
                .id("1")
                .email("john@example.com")
                .firstname("John")
                .roles(List.of(createRole("USER")))
                .lastname("Doe")
                .enabled(false)
                .addresses(new ArrayList<>())
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        APIException exception = assertThrows(NotFoundException.class, () -> {
            addressService.deleteAddress(addressId, keycloakId);
        });

        assertTrue(exception.getMessage().contains("Address"));

        verify(userRepository, times(1)).findByKeycloakId(keycloakId);
    }
}
