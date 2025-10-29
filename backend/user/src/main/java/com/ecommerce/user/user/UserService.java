package com.ecommerce.user.user;

import com.ecommerce.user.address.AddressResponse;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.keycloak.KeyCloakAdminService;
import com.ecommerce.user.keycloak.KeyCloakService;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.role.RoleRepository;
import com.ecommerce.user.shoppingCart.ShoppingCart;
import com.ecommerce.user.shoppingCart.ShoppingCartResponse;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItemResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.Aggregation;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KeyCloakService keyCloakService;
    private final KeyCloakAdminService keyCloakAdminService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse findUserProfile(String keycloakId) {
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User", Optional.empty());
        }
        return mapToUserResponse(userOpt.get());
    }

    @Transactional
    public User updateUserProfile(String currentKeycloakId, UserUpdateRequest request) {

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDateOfBirth(request.getDateOfBirth());

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserProfileByAdmin(String userId, AdminUpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEnabled(request.isEnabled());
        user.setAccountLocked(request.isAccountLocked());

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<Role> roles = roleRepository.findByIdIn(request.getRoleIds());

            if (roles.size() != request.getRoleIds().size()) {
                throw new NotFoundException("One or more roles not found", Optional.empty());
            }

            user.setRoles(roles);

            List<String> roleNames = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            keyCloakAdminService.assignRealmRoleToUser(user.getKeycloakId(), roleNames, userId);
        }

        if(request.getNewPassword() != null && !request.getNewPassword().isBlank()){
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            keyCloakAdminService.updateUserPassword(user.getKeycloakId(), request.getNewPassword());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User addRoleToUser(String userId, String roleId, String currentKeycloakId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role", Optional.of(roleId)));

        boolean userHasRole = user.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));

        String adminToken = keyCloakService.getClientAccessToken();

        if (!userHasRole) {
            user.getRoles().add(role);
            keyCloakService.addRoleToKeycloakUser(user.getKeycloakId(), role.getName(), adminToken);
        } else {
            user.getRoles().removeIf(r -> r.getId().equals(roleId));
            keyCloakService.removeRoleToKeycloakUser(user.getKeycloakId(), role.getName(), adminToken);
        }
        userRepository.save(user);

        return user;
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));
        return mapToUserResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable, String query, String searchBy) {
        Page<User> users;

        if (query == null || query.isBlank()) {
            users = userRepository.findAll(pageable);
        } else if ("userId".equalsIgnoreCase(searchBy)) {
            users = userRepository.findByObjectIdContaining(query, pageable);
        } else {
            users = userRepository.findByEmailContainingIgnoreCase(query, pageable);
        }

        return users.map(this::mapToUserResponse);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
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
