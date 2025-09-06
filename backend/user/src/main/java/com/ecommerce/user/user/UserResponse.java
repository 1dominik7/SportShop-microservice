package com.ecommerce.user.user;

import com.ecommerce.user.address.AddressResponse;
import com.ecommerce.user.shoppingCart.ShoppingCart;
import com.ecommerce.user.shoppingCart.ShoppingCartResponse;
import com.ecommerce.user.userPaymentMethod.UserPaymentMethodResponse;
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
    private boolean accountLocked;
    private boolean enabled;
    private String fullName;
    private String firstname;
    private String lastname;
    private List<String> roleNames;
    private LocalDate dateOfBirth;
    private LocalDateTime createdDate;
    private List<AddressResponse> addresses;
    private List<UserPaymentMethodResponse> userPaymentMethodResponses;
    private ShoppingCartResponse shoppingCart;
}
