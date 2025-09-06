package com.ecommerce.gateway.security;

import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CookieBearerTokenConverter extends ServerBearerTokenAuthenticationConverter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("access_token");
        if (cookie != null) {
            return Mono.just(new BearerTokenAuthenticationToken(cookie.getValue()));
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            return Mono.just(new BearerTokenAuthenticationToken(token));
        }

        return Mono.empty();
    }
}
