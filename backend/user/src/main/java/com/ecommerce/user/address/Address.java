package com.ecommerce.user.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String id;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotBlank(message = "Postal code name cannot be blank")
    private String postalCode;

    @NotBlank(message = "Street name cannot be blank")
    private String street;

    @Pattern(regexp = "^[0-9]{9}$", message = "Phone number must be a 9-digit number")
    private String phoneNumber;

    private String addressLine1;
    private String addressLine2;

}
