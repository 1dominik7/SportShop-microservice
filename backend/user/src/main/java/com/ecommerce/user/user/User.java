package com.ecommerce.user.user;

import com.ecommerce.user.address.Address;
import com.ecommerce.user.role.Role;
import com.ecommerce.user.shoppingCart.ShoppingCart;
import com.ecommerce.user.userPaymentMethod.UserPaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Document(collection = "_users")
public class User{

    @Id
    private String id;
    private String keycloakId;

    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String password;
    private boolean accountLocked;
    private boolean enabled;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @DBRef
    private List<Role> roles;

    private List<Address> addresses;

    private ShoppingCart shoppingCart;

    private List<UserPaymentMethod> userPaymentMethods;

    @Builder.Default
    private List<Token> tokens = new ArrayList<>();

    public String getFullName(){
        return firstname + " " + lastname;
    }

    public String getId() {
        return id;
    }
}
