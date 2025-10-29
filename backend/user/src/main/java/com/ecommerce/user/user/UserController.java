package com.ecommerce.user.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile(@AuthenticationPrincipal Jwt jwt){
        String currentKeycloakId = jwt.getSubject();
        UserResponse userOpt = userService.findUserProfile(currentKeycloakId);

        return ResponseEntity.ok(userOpt);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(@RequestBody UserUpdateRequest request,
                                                  @AuthenticationPrincipal Jwt jwt
                                                  ){
        String currentKeycloakId = jwt.getSubject();
        User updatedUser = userService.updateUserProfile(currentKeycloakId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<User> updateUserProfileByAdmin(
            @PathVariable String userId,
            @RequestBody AdminUpdateUserRequest request,
                                                  @AuthenticationPrincipal Jwt jwt
    ){
        User updatedUser = userService.updateUserProfileByAdmin(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/{roleId}")
    public ResponseEntity<User> addRoleToUser(@PathVariable String userId,
                                              @PathVariable String roleId,
                                              @AuthenticationPrincipal Jwt jwt){
        String currentKeycloakId = jwt.getSubject();
        User updatedRoleUser = userService.addRoleToUser(userId, roleId, currentKeycloakId);
        return ResponseEntity.ok(updatedRoleUser);
    }

    @GetMapping("/byUserId/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId){
        UserResponse userOpt = userService.getUserById(userId);
        return ResponseEntity.ok(userOpt);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "id") String sortBy,
                                            @RequestParam(defaultValue = "desc") String direction,
                                            @RequestParam(required=false) String query,
                                            @RequestParam(required=false) String searchBy){
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.getAllUsers(pageable, query, searchBy);
        return ResponseEntity.ok(users);
    }
}

