package com.ecommerce.user.auth;

import com.ecommerce.user.user.UserResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private UserResponse user;
}
