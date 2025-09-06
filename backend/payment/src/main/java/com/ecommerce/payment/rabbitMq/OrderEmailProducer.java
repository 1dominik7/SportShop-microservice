package com.ecommerce.payment.rabbitMq;

import com.ecommerce.payment.payment.OrderConfirmationEmailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEmailProducer {

    private final StreamBridge streamBridge;

    public void sendOrderEmail(OrderConfirmationEmailPayload payload){
        streamBridge.send("orderEmail-out-0", payload);
    }
}
