package com.ecommerce.order.clients;

import com.ecommerce.order.clients.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="user-service", url = "${USER_SERVICE_URL}")
public interface UserClient {

    @GetMapping("/users/profile")
    UserResponse getUserProfile(@RequestHeader("Authorization") String authorizationHeader);


    @DeleteMapping("/cart")
    ResponseEntity<String> clearUseCart(@RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/users/byUserId/{userId}")
    UserResponse getUserById(@PathVariable String userId);
}
