package com.ecommerce.marketing.config;

import com.ecommerce.marketing.newsletter.NewsletterEmailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsletterProducer {

    private final StreamBridge streamBridge;

    public void sendNewsletterEmail(NewsletterEmailPayload payload) {
        streamBridge.send("newsletterEmail-out-0", payload);
    }
}
