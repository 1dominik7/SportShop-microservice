package com.ecommerce.user.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

    @Email(message = "Email is not formatted")
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email is required")
    private String email;
    @NotEmpty(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min=8, message = "Password should 8 characters long minimum")
    private String password;

}
