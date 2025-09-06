package com.ecommerce.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@EnableAutoConfiguration(exclude = {
		MailSenderAutoConfiguration.class,
		SecurityAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class PaymentApplicationTests {

	@DynamicPropertySource
	static void mongoDbProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27017/testdb");
	}

	@Test
	void contextLoads() {
	}

}
