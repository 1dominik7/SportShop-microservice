package com.ecommerce.user.user;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
}
