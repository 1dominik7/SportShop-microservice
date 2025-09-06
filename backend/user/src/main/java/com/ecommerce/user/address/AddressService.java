package com.ecommerce.user.address;

import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserRepository userRepository;

    @Transactional
    public AddressResponse createAddress(AddressRequest addressRequest, String currentKeycloakId) {

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        Address newAddress = Address.builder()
                .id(UUID.randomUUID().toString())
                .country(addressRequest.getCountry())
                .firstName(addressRequest.getFirstName())
                .lastName(addressRequest.getLastName())
                .city(addressRequest.getCity())
                .postalCode(addressRequest.getPostalCode())
                .street(addressRequest.getStreet())
                .phoneNumber(addressRequest.getPhoneNumber())
                .addressLine1(addressRequest.getAddressLine1())
                .addressLine2(addressRequest.getAddressLine2())
                .build();

        if (user.getAddresses() == null) {
            user.setAddresses(new ArrayList<>());
        }

        user.getAddresses().add(newAddress);
        userRepository.save(user);

        return mapToResponse(newAddress);
    }

    public List<AddressResponse> getUserAddresses(String currentKeycloakId) {

        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        List<Address> addresses = Optional.ofNullable(user.getAddresses())
                .orElse(Collections.emptyList());

        return addresses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse getAddressById(String addressId, String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        List<Address> addresses = Optional.ofNullable(user.getAddresses())
                .orElse(Collections.emptyList());

        Address address = addresses .stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Address",Optional.of(addressId)));
        return mapToResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(String addressId, AddressRequest addressRequest, String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        List<Address> addresses = user.getAddresses();
        if (addresses == null) {
            throw new APIException("User has no addresses");
        }
        Address address = addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Address", Optional.of(addressId)));

        address.setCountry(addressRequest.getCountry());
        address.setCity(addressRequest.getCity());
        address.setFirstName(addressRequest.getFirstName());
        address.setLastName(addressRequest.getLastName());
        address.setPostalCode(addressRequest.getPostalCode());
        address.setStreet(addressRequest.getStreet());
        address.setPhoneNumber(addressRequest.getPhoneNumber());
        address.setAddressLine1(addressRequest.getAddressLine1());
        address.setAddressLine2(addressRequest.getAddressLine2());

        userRepository.save(user);

        return mapToResponse(address);
    }

    @Transactional
    public void deleteAddress(String addressId, String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        List<Address> addresses = user.getAddresses();
        if (addresses == null) {
            throw new APIException("User has no addresses");
        }

        boolean removed = user.getAddresses().removeIf(a -> a.getId().equals(addressId));
        if(!removed) throw new NotFoundException("Address", Optional.of(addressId));

        userRepository.save(user);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
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
                .build();
    }
}
