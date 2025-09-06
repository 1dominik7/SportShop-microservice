package com.ecommerce.payment.clients;

import com.ecommerce.payment.clients.dto.UserPaymentMethodResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="user-service", url = "${USER_SERVICE_URL}")
public interface UserClient {

        @GetMapping("/user-payment-method/{paymentMethodId}")
        UserPaymentMethodResponse getUserPaymentMethodById(@PathVariable String paymentMethodId,
                                                       @RequestHeader("Authorization") String jwt);


}
