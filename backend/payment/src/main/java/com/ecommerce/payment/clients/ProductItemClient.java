package com.ecommerce.payment.clients;

import com.ecommerce.payment.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.payment.clients.dto.ProductStockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="product-service", url = "${PRODUCT_SERVICE_URL}")
public interface ProductItemClient {

    @PutMapping("/productItems/update-stock")
    void updateProductItemStock(@RequestBody List<ProductStockUpdateRequest> updates, @RequestHeader("Authorization") String authorizationHeader);

    @GetMapping("/productItems/byProductItemIds")
    List<ProductItemOneByColourResponse> getProductItemsByIds(@RequestParam List<Integer> productItemIds);
}
