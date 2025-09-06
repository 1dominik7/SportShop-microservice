package com.ecommerce.marketing.clients;

import com.ecommerce.marketing.clients.dto.DiscountCodeRequest;
import com.ecommerce.marketing.clients.dto.DiscountCodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="user-service", url = "${USER_SERVICE_URL}")
public interface UserClient {

    @PostMapping("/discount")
    DiscountCodeResponse createDiscountCode(@RequestBody DiscountCodeRequest discountCodeRequest);
}
