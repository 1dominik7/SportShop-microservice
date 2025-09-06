package com.ecommerce.user.user;

import com.ecommerce.user.address.AddressResponse;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.role.RoleRepository;
import com.ecommerce.user.shoppingCart.ShoppingCart;
import com.ecommerce.user.shoppingCart.ShoppingCartResponse;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KeyCloakService keyCloakService;

    public UserResponse findUserProfile(String keycloakId) {
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        if(userOpt.isEmpty()){
            throw new NotFoundException("User", Optional.empty());
        }
        return mapToUserResponse(userOpt.get());
    }

    @Transactional
    public User updateUserProfile(String currentKeycloakId, UserUpdateRequest request){

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDateOfBirth(request.getDateOfBirth());

        return userRepository.save(user);
    }

    @Transactional
    public User addRoleToUser(String userId, String roleId, String currentKeycloakId){

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role", Optional.of(roleId)));

        boolean userHasRole = user.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));

        String adminToken = keyCloakService.getClientAccessToken();

        if(!userHasRole){
            user.getRoles().add(role);
            keyCloakService.addRoleToKeycloakUser(user.getKeycloakId(), role.getName(), adminToken);
        } else{
            user.getRoles().removeIf(r -> r.getId().equals(roleId));
            keyCloakService.removeRoleToKeycloakUser(user.getKeycloakId(), role.getName(), adminToken);
        }
            userRepository.save(user);

        return user;
    }

    public UserResponse getUserById(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user){
        return  UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .fullName(user.getFullName())
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
                .build();
    }
}
