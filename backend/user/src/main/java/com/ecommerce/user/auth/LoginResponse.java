package com.ecommerce.user.auth;

import com.ecommerce.user.user.UserResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private UserResponse user;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
}
