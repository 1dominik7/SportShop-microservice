package com.ecommerce.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder
                .withJwkSetUri("http://keycloak:8080/realms/ecom-app/protocol/openid-connect/certs")
                .build();
    }
}
