package com.ecommerce.user.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
}

