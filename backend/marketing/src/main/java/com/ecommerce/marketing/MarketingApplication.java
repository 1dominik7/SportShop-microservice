package com.ecommerce.marketing;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableRabbit
public class MarketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketingApplication.class, args);
	}

}
