package com.ecommerce.marketing.config;

import com.ecommerce.marketing.email.EmailService;
import com.ecommerce.marketing.email.EmailTemplateName;
import com.ecommerce.marketing.newsletter.NewsletterEmailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class NewsletterConsumer {

    private final EmailService emailService;

    @Bean
    public Consumer<NewsletterEmailPayload> newsletterEmail() {
        return payload -> {
            try {
                emailService.sendEmail(
                        payload.getEmail(),
                        EmailTemplateName.NEWSLETTER,
                        payload.getDiscountCode(),
                        "Welcome to SportShop Newsletter - Your Discount Code"
                );
            } catch (Exception e) {
                System.err.println("Error during sending newsletter email: " + e.getMessage());
            }
        };
    }
}
