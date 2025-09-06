package com.ecommerce.payment.kafka;

import com.ecommerce.payment.clients.dto.ProductStockBatchUpdateRequest;
import com.ecommerce.payment.clients.dto.ProductStockUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaProducers {
    private final StreamBridge streamBridge;

    public void sendUpdateStock(List<ProductStockUpdateRequest> requests) {
        ProductStockBatchUpdateRequest batch = new ProductStockBatchUpdateRequest(requests);
        streamBridge.send("stockUpdate-out-0", batch);
    }
}
