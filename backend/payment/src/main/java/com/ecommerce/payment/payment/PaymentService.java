package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.*;
import com.ecommerce.payment.clients.dto.*;
import com.ecommerce.payment.kafka.KafkaProducers;
import com.ecommerce.payment.exceptions.APIException;
import com.ecommerce.payment.exceptions.NotFoundException;
import com.ecommerce.payment.rabbitMq.OrderEmailProducer;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InsufficientResourcesException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${application.stripe.secret-key}")
    private String stripeSecretKey;

    private final PaymentRepository paymentRepository;
    private final ShopOrderCallerService shopOrderCallerService;
    private final ProductItemCallerService productItemCallerService;
    private final OrderEmailProducer orderEmailProducer;
    private final KafkaProducers kafkaProducers;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) throws InsufficientResourcesException {

        if (request.getShopOrderId() == null) {
            throw new IllegalArgumentException("Shop order is required");
        }

        if (request.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
            throw new APIException("Payment not completed successfully. Current status: " + request.getStatus());
        }

        Payment payment = Payment.builder()
                .transactionId(request.getTransactionId())
                .paymentIntentId(request.getPaymentIntentId())
                .shopOrderId(request.getShopOrderId())
                .provider(request.getProvider())
                .last4CardNumber(request.getLast4CardNumber())
                .paymentDate(request.getPaymentDate())
                .status(request.getStatus())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .id(savedPayment.getId())
                .transactionId(savedPayment.getTransactionId())
                .paymentIntentId(savedPayment.getPaymentIntentId())
                .shopOrderId(savedPayment.getShopOrderId())
                .provider(savedPayment.getProvider())
                .last4CardNumber(savedPayment.getLast4CardNumber())
                .paymentDate(savedPayment.getPaymentDate())
                .createdAt(savedPayment.getCreatedAt())
                .updatedAt(savedPayment.getUpdatedAt())
                .status(savedPayment.getStatus())
                .build();
    }

    @Transactional
    public PaymentVerificationResponse verifyPayment(String sessionId, Jwt jwt) throws StripeException, InsufficientResourcesException {

        Session session = Session.retrieve(sessionId);
        String paymentIntentId = session.getPaymentIntent();
        String orderIdStr = session.getMetadata().get("order_id");
        Integer orderId = Integer.parseInt(orderIdStr);
        String userEmail = jwt.getClaimAsString("email");

        ShopOrderResponse shopOrder = shopOrderCallerService.getUserShopOrderById(orderId, jwt);

        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new NotFoundException("Payment", Optional.empty()));

        PaymentIntent intent = PaymentIntent.retrieve(payment.getPaymentIntentId());

        if ("succeeded".equals(intent.getStatus())) {

            if (shopOrder.getPaymentStatus() == Payment.PaymentStatus.SUCCEEDED) {
                return PaymentVerificationResponse.builder()
                        .id(shopOrder.getPaymentId())
                        .transactionId(intent.getId())
                        .paymentIntentId(paymentIntentId)
                        .shopOrder(shopOrder)
                        .createdAt(shopOrder.getPaymentCreatedAt())
                        .updatedAt(shopOrder.getPaymentUpdatedAt())
                        .status(shopOrder.getPaymentStatus())
                        .build();
            }

            PaymentMethod paymentMethod = PaymentMethod.retrieve(intent.getPaymentMethod());
            String cardBrand = paymentMethod.getCard().getBrand();

            ShopOrderPaymentUpdateRequest updateRequest = new ShopOrderPaymentUpdateRequest();
            updateRequest.setPaymentId(payment.getId());
            updateRequest.setPaymentStatus(Payment.PaymentStatus.SUCCEEDED);
            updateRequest.setPaymentIntentId(paymentIntentId);
            updateRequest.setPaymentTransactionId(sessionId);
            updateRequest.setPaymentMethodName(cardBrand);
            updateRequest.setOrderStatus("packing");
            updateRequest.setPaymentCreatedAt(LocalDateTime.now());

            shopOrder = shopOrderCallerService.updateShopOrder(orderId, updateRequest, jwt);

            refreshProductStock(shopOrder, jwt);
            updateProductStock(shopOrder, jwt);

            orderEmailProducer.sendOrderEmail(
                    new OrderConfirmationEmailPayload(
                            shopOrder.getId(),
                            userEmail,
                            shopOrder.getOrderLines(),
                            shopOrder.getOrderDate(),
                            shopOrder.getFinalOrderTotal(),
                            shopOrder.getShippingMethod()
                    )
            );

            return PaymentVerificationResponse.builder()
                    .id(shopOrder.getPaymentId())
                    .transactionId(intent.getId())
                    .paymentIntentId(paymentIntentId)
                    .shopOrder(shopOrder)
                    .createdAt(shopOrder.getPaymentCreatedAt())
                    .updatedAt(shopOrder.getPaymentUpdatedAt())
                    .status(shopOrder.getPaymentStatus())
                    .build();
        }
        return PaymentVerificationResponse.builder()
                .status(Payment.PaymentStatus.FAILED)
                .build();
    }

    @Transactional
    public PaymentVerificationResponse verifyPayUPayment(Integer orderId, Jwt jwt) throws InsufficientResourcesException {

        Payment payment = paymentRepository.findByShopOrderId(orderId)
                    .orElseThrow(() -> new NotFoundException("Payment for transaction: " + orderId, Optional.empty()));

            String userEmail = jwt.getClaimAsString("email");

            ShopOrderResponse shopOrder = shopOrderCallerService.getUserShopOrderById(orderId, jwt);

            if (shopOrder.getPaymentStatus() == Payment.PaymentStatus.SUCCEEDED) {
                return PaymentVerificationResponse.builder()
                        .id(shopOrder.getPaymentId())
                        .transactionId(payment.getTransactionId())
                        .paymentIntentId(payment.getPaymentIntentId())
                        .shopOrder(shopOrder)
                        .createdAt(shopOrder.getPaymentCreatedAt())
                        .updatedAt(shopOrder.getPaymentUpdatedAt())
                        .status(shopOrder.getPaymentStatus())
                        .build();
            }

            ShopOrderPaymentUpdateRequest updateRequest = new ShopOrderPaymentUpdateRequest();
            updateRequest.setPaymentId(payment.getId());
            updateRequest.setPaymentStatus(Payment.PaymentStatus.SUCCEEDED);
            updateRequest.setPaymentIntentId(payment.getPaymentIntentId());
            updateRequest.setPaymentTransactionId(payment.getTransactionId());
            updateRequest.setPaymentMethodName(payment.getProvider());
            updateRequest.setOrderStatus("packing");
            updateRequest.setPaymentCreatedAt(LocalDateTime.now());

            shopOrder = shopOrderCallerService.updateShopOrder(orderId, updateRequest, jwt);

            refreshProductStock(shopOrder, jwt);
            updateProductStock(shopOrder, jwt);

            orderEmailProducer.sendOrderEmail(
                    new OrderConfirmationEmailPayload(
                            shopOrder.getId(),
                            userEmail,
                            shopOrder.getOrderLines(),
                            shopOrder.getOrderDate(),
                            shopOrder.getFinalOrderTotal(),
                            shopOrder.getShippingMethod()
                    )
            );

            return PaymentVerificationResponse.builder()
                    .id(shopOrder.getPaymentId())
                    .transactionId(payment.getTransactionId())
                    .paymentIntentId(payment.getPaymentIntentId())
                    .shopOrder(shopOrder)
                    .createdAt(shopOrder.getPaymentCreatedAt())
                    .updatedAt(shopOrder.getPaymentUpdatedAt())
                    .status(shopOrder.getPaymentStatus())
                    .build();
    }

    private void updateProductStock(ShopOrderResponse order, Jwt jwt) throws InsufficientResourcesException {
        List<ProductStockUpdateRequest> updateRequests = new ArrayList<>();

        for (OrderLineResponse orderLine : order.getOrderLines()) {
            ProductItemToOrderResponse productItem = orderLine.getProductItem();
            int orderedQty = orderLine.getQty();

            if (productItem.getQtyInStock() < orderedQty) {
                throw new InsufficientResourcesException("Not enough stock for product: " + productItem.getId());
            }

            updateRequests.add(new ProductStockUpdateRequest(productItem.getId(), orderedQty));
        }

        kafkaProducers.sendUpdateStock(updateRequests);
    }

    private void refreshProductStock(ShopOrderResponse order, Jwt jwt) {
        List<Integer> productItemIds = order.getOrderLines().stream()
                .map(line -> line.getProductItem().getId())
                .collect(Collectors.toList());

        List<ProductItemOneByColourResponse> responses = productItemCallerService.getProductItemsByIds(productItemIds);

        Map<Integer, Integer> stockMap = new HashMap<>();
        for (ProductItemOneByColourResponse resp : responses) {
            if (resp.getProductItemOneByColour() != null) {
                for (ProductItemOneByColour item : resp.getProductItemOneByColour()) {
                    stockMap.put(item.getId(), item.getQtyInStock());
                }
            }
        }

        for (OrderLineResponse orderLine : order.getOrderLines()) {
            var productItem = orderLine.getProductItem();
            if (productItem != null) {
                Integer currentStock = stockMap.get(productItem.getId());
                productItem.setQtyInStock(currentStock != null ? currentStock : 0);
            }
        }
    }
}
