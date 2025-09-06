package com.ecommerce.user.address;

import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @Mock
    private Jwt jwt;

    @Test
    void AddressController_CreateAddress_Success() throws Exception {
        String keycloakId = "keycloak-123";

        AddressRequest addressRequest = AddressRequest.builder()
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

        AddressResponse addressResponse = AddressResponse.builder()
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

        when(addressService.createAddress(any(AddressRequest.class), eq(keycloakId))).thenReturn(addressResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(addressRequest);

        mockMvc.perform(post("/address/create")
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId)))
                        .contentType("application/json")
                        .content(jsonRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.country").value("Poland"))
                .andExpect(jsonPath("$.city").value("Warsaw"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.postalCode").value("01-001"))
                .andExpect(jsonPath("$.street").value("Street"))
                .andExpect(jsonPath("$.phoneNumber").value("123123123"))
                .andExpect(jsonPath("$.addressLine1").value("63"))
                .andExpect(jsonPath("$.addressLine2").value("2/3"));
    }

    @Test
    void AddressController_UpdateAddress_Success() throws Exception {
        String keycloakId = "keycloak-123";
        String addressId = "1";

        AddressRequest updateAddressRequest = AddressRequest.builder()
                .country("Poland")
                .city("Warsaw1")
                .firstName("John1")
                .lastName("Doe1")
                .postalCode("02-002")
                .street("Street1")
                .phoneNumber("123123121")
                .addressLine1("61")
                .addressLine2("2/1")
                .build();

        AddressResponse updatedAddress  = AddressResponse.builder()
                .id("1")
                .country("Poland")
                .city("Warsaw1")
                .firstName("John1")
                .lastName("Doe1")
                .postalCode("02-002")
                .street("Street1")
                .phoneNumber("123123121")
                .addressLine1("61")
                .addressLine2("2/1")
                .build();

        when(addressService.updateAddress(eq(addressId), any(AddressRequest.class), eq(keycloakId))).thenReturn(updatedAddress);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(updateAddressRequest);

        mockMvc.perform(put("/address/{addressId}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId)))
                        .contentType("application/json")
                        .content(jsonRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value(updatedAddress.getCountry()))
                .andExpect(jsonPath("$.city").value(updatedAddress.getCity()))
                .andExpect(jsonPath("$.firstName").value(updatedAddress.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(updatedAddress.getLastName()))
                .andExpect(jsonPath("$.postalCode").value(updatedAddress.getPostalCode()))
                .andExpect(jsonPath("$.street").value(updatedAddress.getStreet()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedAddress.getPhoneNumber()))
                .andExpect(jsonPath("$.addressLine1").value(updatedAddress.getAddressLine1()))
                .andExpect(jsonPath("$.addressLine2").value(updatedAddress.getAddressLine2()));
    }

    @Test
    void AddressController_DeleteAddress_Success() throws Exception {
        String keycloakId = "keycloak-123";
        String addressId = "1";

        AddressResponse address  = AddressResponse.builder()
                .id("1")
                .country("Poland")
                .city("Warsaw1")
                .firstName("John1")
                .lastName("Doe1")
                .postalCode("02-002")
                .street("Street1")
                .phoneNumber("123123121")
                .addressLine1("61")
                .addressLine2("2/1")
                .build();

        doNothing().when(addressService).deleteAddress(eq(addressId),  eq(keycloakId));

        mockMvc.perform(delete("/address/{addressId}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(keycloakId)))
                )
                .andExpect(status().isOk())

                .andExpect(content().string("Address has been successfully deleted!"));
                verify(addressService, times(1)).deleteAddress(eq(addressId), eq(keycloakId));
    }
}
