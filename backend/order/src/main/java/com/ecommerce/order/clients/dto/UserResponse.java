package com.ecommerce.order.clients.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String keycloakId;
    private boolean accountLocked;
    private boolean enabled;
    private String fullName;
    private List<String> roleNames;
    private LocalDate dateOfBirth;
    private LocalDateTime createdDate;
    private List<AddressResponse> addresses;
    private ShoppingCartResponse shoppingCart;
}
