package com.ecommerce.marketing.newsletter;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/create")
    public ResponseEntity<NewsletterResponse> addUserToNewsletter(@RequestBody NewsletterRequest newsletterRequest){
        NewsletterResponse newsletterResponse = newsletterService.addUserToNewsletter(newsletterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newsletterResponse);
    }
}
