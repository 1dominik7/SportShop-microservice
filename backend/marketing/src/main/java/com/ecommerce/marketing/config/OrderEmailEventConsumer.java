package com.ecommerce.marketing.config;

import com.ecommerce.marketing.config.dto.OrderConfirmationEmailPayload;
import com.ecommerce.marketing.email.EmailService;
import com.ecommerce.marketing.email.EmailTemplateName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class OrderEmailEventConsumer {

    private final EmailService emailService;

    @Bean
    public Consumer<OrderConfirmationEmailPayload> orderEmail() {
        return payload -> {
            try{
                emailService.sendOrderConfirmationEmail(
                        EmailTemplateName.ORDER_EMAIL,
                        payload.getEmail(),
                        payload.getOrderLines(),
                        payload.getOrderId(),
                        payload.getOrderDate(),
                        payload.getTotalPrice(),
                        payload.getShippingMethodResponse(),
                        "Your Order Confirmation from SportShop"
                );
            } catch(Exception e){
                System.err.println("Error during sending order confirmation email: " + e.getMessage());
            }
        };
    }

}
