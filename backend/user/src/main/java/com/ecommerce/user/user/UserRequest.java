package com.ecommerce.user.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    private String email;
    private String firstname;
    private String lastname;
    private String username;
    private String password;

}
