package com.ecommerce.user.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
