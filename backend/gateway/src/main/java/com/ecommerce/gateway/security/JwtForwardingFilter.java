package com.ecommerce.gateway.security;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Configuration
public class JwtForwardingFilter {
    @Bean
    public GlobalFilter authorizationHeaderFilter() {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var cookies = request.getCookies();
            var accessTokenCookie = cookies.getFirst("access_token");

            if (accessTokenCookie != null) {
                String token = accessTokenCookie.getValue();
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("Authorization", "Bearer " + token)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }

            return chain.filter(exchange);
        };
    }
}
