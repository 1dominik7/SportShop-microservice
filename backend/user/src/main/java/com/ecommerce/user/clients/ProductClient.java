package com.ecommerce.user.clients;

import com.ecommerce.user.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.user.clients.dto.ProductResponseGetById;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="product-service", url = "${PRODUCT_SERVICE_URL}")
public interface ProductClient {

    @GetMapping("/productItems/{productItemId}")
    ProductItemOneByColourResponse getProductItemById(@PathVariable Integer productItemId,
                                                      @RequestParam(value = "colour", required = false) String colour);

    @GetMapping("/productItems/byIds")
    List<ProductItemOneByColourResponse> getProductItemByIds(@RequestParam List<Integer> productItemIds);

    @GetMapping("/products/{id}")
    ProductResponseGetById getProductById(@PathVariable Integer id);

    @GetMapping("/productItems/statistics/totalProducts")
    Long getTotalProductItemsNumber();
}
