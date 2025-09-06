package com.ecommerce.user.clients;

import com.ecommerce.user.clients.dto.PaymentTypeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name="payment-service", url = "${PAYMENT_SERVICE_URL}")
public interface PaymentClient {

    @GetMapping("/payment-type/all")
    List<PaymentTypeResponse> paymentTypeResponse();

}
