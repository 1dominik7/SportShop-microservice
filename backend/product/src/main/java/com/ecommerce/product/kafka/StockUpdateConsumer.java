package com.ecommerce.product.kafka;

import com.ecommerce.product.product.productItem.ProductItemService;
import com.ecommerce.product.product.productItem.request.ProductStockBatchUpdateRequest;
import com.ecommerce.product.product.productItem.request.ProductStockUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class StockUpdateConsumer {
    private final KafkaTemplate<String, com.ecommerce.product.product.productItem.request.ProductStockBatchUpdateRequest> kafkaTemplate;

    private final ProductItemService productItemService;

    @Bean
    public Consumer<ProductStockBatchUpdateRequest> stockUpdate(){
        return batchUpdateRequest -> {
            List<ProductStockUpdateRequest> requests = batchUpdateRequest.getUpdates();
            productItemService.updateStock(requests);
        };
    }
}
