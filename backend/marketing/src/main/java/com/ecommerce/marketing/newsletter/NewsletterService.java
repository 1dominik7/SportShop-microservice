package com.ecommerce.marketing.newsletter;

import com.ecommerce.marketing.clients.UserCallerService;
import com.ecommerce.marketing.clients.dto.DiscountCodeRequest;
import com.ecommerce.marketing.clients.dto.DiscountCodeResponse;
import com.ecommerce.marketing.config.NewsletterProducer;
import com.ecommerce.marketing.exceptions.APIException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;
    private final UserCallerService userCallerService;
    private final NewsletterProducer newsletterProducer;

    @Transactional
    public NewsletterResponse addUserToNewsletter(NewsletterRequest newsletterRequest) {

        boolean userIsAlreadySubscribed = newsletterRepository.existsByEmail(newsletterRequest.getEmail());

        if (userIsAlreadySubscribed == true) {
            throw new APIException("This email is already in newsletter.");
        }

        String code = generateRandomCode(10);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        DiscountCodeResponse discountCodeResponse = userCallerService.createDiscountCode(
                DiscountCodeRequest.builder()
                        .name("Newsletter for " + newsletterRequest.getEmail())
                        .code(code)
                        .discount(10)
                        .expiryDate(expiryDate)
                        .singleUse(true)
                        .build()
        );

        if (discountCodeResponse == null || discountCodeResponse.getId() == null) {
            throw new APIException("Failed to generate discount code");
        }

        Newsletter newsletter = Newsletter.builder()
                .email(newsletterRequest.getEmail())
                .couponId(discountCodeResponse.getId())
                .subscribedAt(LocalDateTime.now().toString())
                .build();

        newsletterRepository.save(newsletter);

        newsletterProducer.sendNewsletterEmail(
                new NewsletterEmailPayload(
                        newsletter.getId(),
                        newsletter.getEmail(),
                        code
                ));

        return NewsletterResponse.builder()
                .id(newsletter.getId())
                .email(newsletter.getEmail())
                .couponId(discountCodeResponse.getId())
                .email(newsletter.getEmail())
                .subscribedAt(LocalDateTime.now().toString())
                .build();
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
