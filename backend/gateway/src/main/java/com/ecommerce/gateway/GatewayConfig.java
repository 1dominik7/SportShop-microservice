package com.ecommerce.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${EUREKA_URL}")
    private String eurekaUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route("product-service", r -> r
                        .path("/api/v1/products/**", "/api/v1/category/**","/api/v1/productItems/**","/api/v1/variation/**","/api/v1/variation-option/**")
                        .uri("lb://PRODUCT-SERVICE"))
                .route("user-service", r -> r
                        .path("/api/v1/users/**", "/api/v1/auth/**", "/api/v1/address/**","/api/v1/discount/**","/api/v1/review/**", "/api/v1/cart/**","/api/v1/user-payment-method/**")
                        .uri("lb://USER-SERVICE"))
                .route("order-service", r -> r
                        .path("/api/v1/shipping-method/**", "/api/v1/shop-order/**","/api/v1/order-line/**")
                        .uri("lb://ORDER-SERVICE"))
                .route("payment-service", r -> r
                        .path("/api/v1/payment/**", "/api/v1/payment-type/**")
                        .uri("lb://PAYMENT-SERVICE"))
                .route("marketing-service", r -> r
                        .path("/api/v1/newsletter/**", "/api/v1/main-images/**")
                        .uri("lb://MARKETING-SERVICE"))
                .route("eureka-service", r -> r
                        .path("/eureka/main")
                        .uri(eurekaUrl.replace("/eureka/", "")))
                .build();
    }
}
