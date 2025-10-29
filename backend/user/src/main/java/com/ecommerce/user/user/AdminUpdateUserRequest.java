package com.ecommerce.user.user;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUpdateUserRequest {
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    private boolean enabled;
    private boolean accountLocked;
    private String newPassword;
    private List<String> roleIds;
}
