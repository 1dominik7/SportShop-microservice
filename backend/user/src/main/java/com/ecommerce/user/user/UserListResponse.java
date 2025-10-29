package com.ecommerce.user.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserListResponse {
    private List<UserResponse> content;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
