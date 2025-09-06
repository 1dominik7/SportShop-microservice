package com.ecommerce.product.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs")
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.GET, "/category/**").permitAll()
                                .requestMatchers(HttpMethod.GET,"/productItems/**").permitAll()
                                .requestMatchers(HttpMethod.GET,"/products/**").permitAll()
                                .requestMatchers(HttpMethod.GET,"/variation/**").permitAll()
                                .requestMatchers(HttpMethod.GET,"/variation-option/**").permitAll()
                                .requestMatchers(HttpMethod.POST,"/category/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/category/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/category/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST,"/productItems/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/productItems/**").hasAnyRole("ADMIN","USER")
                                .requestMatchers(HttpMethod.DELETE,"/productItems/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST,"/products/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/products/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/products/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST,"/variation/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/variation/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/variation/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST,"/variation-option/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/variation-option/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/variation-option/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

                List<String> roles = new ArrayList<>();

                if (resourceAccess != null && resourceAccess.containsKey("admin-client")) {
                    Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("admin-client");
                    if (clientAccess != null && clientAccess.containsKey("roles")) {
                        roles = (List<String>) clientAccess.get("roles");
                    }
                }

                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                return new JwtAuthenticationToken(jwt, authorities);
            }
        };
    }}