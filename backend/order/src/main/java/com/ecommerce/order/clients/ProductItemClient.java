package com.ecommerce.order.clients;

import com.ecommerce.order.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.order.clients.dto.ProductItemResponse;
import com.ecommerce.order.clients.dto.ProductItemToOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="product-service", url = "${PRODUCT_SERVICE_URL}")
public interface ProductItemClient {

    @GetMapping("/productItems/byId/{productItemId}")
    ProductItemResponse getProductItemById(@PathVariable Integer productItemId);

    @GetMapping("/productItems/byProductItemIds")
    List<ProductItemOneByColourResponse> getProductItemByIdsToCreateOrder(@RequestParam List<Integer> productItemIds);

    @GetMapping("/productItems/by-product-items-ids/shop-order")
    List<ProductItemToOrderResponse> getProductItemByIdsToOrders(@RequestParam List<Integer> productItemIds);
}
