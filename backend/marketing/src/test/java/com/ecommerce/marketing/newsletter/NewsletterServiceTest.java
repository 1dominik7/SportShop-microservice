package com.ecommerce.marketing.newsletter;

import com.ecommerce.marketing.clients.UserCallerService;
import com.ecommerce.marketing.clients.dto.DiscountCodeResponse;
import com.ecommerce.marketing.config.NewsletterProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NewsletterServiceTest {

    @Mock
    private NewsletterRepository newsletterRepository;

    @Mock
    private UserCallerService userCallerService;

    @Mock
    private NewsletterProducer newsletterProducer;

    @InjectMocks
    private NewsletterService newsletterService;

    @Test
    void NewsletterService_AddUserToNewsletter_Success() {

    NewsletterRequest newsletterRequest = NewsletterRequest.builder()
            .email("test@gmail.com")
            .build();

    when(newsletterRepository.existsByEmail(newsletterRequest.getEmail())).thenReturn(false);

        DiscountCodeResponse discountCodeResponse = DiscountCodeResponse.builder()
                .id("discount-1")
                .code("discount")
                .discount(10)
                .build();

        when(userCallerService.createDiscountCode(any())).thenReturn(discountCodeResponse);

        Newsletter newsletter = Newsletter.builder()
                .id(1)
                .email(newsletterRequest.getEmail())
                .couponId(discountCodeResponse.getId())
                .subscribedAt(LocalDateTime.now().toString())
                .build();

        when(newsletterRepository.save(any())).thenReturn(newsletter);

        NewsletterResponse response = newsletterService.addUserToNewsletter(newsletterRequest);

        assertNotNull(response);
        assertEquals(newsletterRequest.getEmail(), response.getEmail());
        assertEquals(discountCodeResponse.getId(), response.getCouponId());
        verify(newsletterProducer, times(1)).sendNewsletterEmail(any());
    }


}
