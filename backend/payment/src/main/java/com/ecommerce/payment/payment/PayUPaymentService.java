package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.ProductItemCallerService;
import com.ecommerce.payment.clients.dto.OrderLineResponse;
import com.ecommerce.payment.clients.dto.ProductItemOneByColour;
import com.ecommerce.payment.clients.dto.ProductItemOneByColourResponse;
import com.ecommerce.payment.clients.dto.ShopOrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.naming.InsufficientResourcesException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayUPaymentService {

    private final PaymentService paymentService;
    private final RestTemplate restTemplate;
    private final ProductItemCallerService productItemCallerService;
    private final ObjectMapper objectMapper;

//    @Value("${application.tpay.client-id}")
//    private String clientId;

//    @Value("${application.tpay.client-secret}")
//    private String clientSecret;

//    @Value("${application.tpay.api-url}")
//    private String apiUrl;

    private String clientId = "497723";
    private String clientSecret = "c7bef1c5282b371885b95f78b1f7bcb3";
    private String posId = "497723";
    private String apiUrl = "https://secure.snd.payu.com";

    private String getAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            String authUrl = apiUrl + "/pl/standard/user/oauth/authorize";

            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = (String) responseBody.get("access_token");
                return accessToken;
            } else {
                log.error("OAuth failed: {} - {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("OAuth failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error getting access token: {}", e.getMessage());
            throw new RuntimeException("Access token error: " + e.getMessage());
        }
    }

    public String createCheckoutSession(ShopOrderResponse order, String successUrl, String cancelUrl) {
        try {
            String accessToken = getAccessToken();

            Map<String, Object> orderRequest = new HashMap<>();

            Double amountUsd = order.getFinalOrderTotal();
            Double amountPln = amountUsd * 4;

            orderRequest.put("notifyUrl", "https://webhook.site/d7341d1a-a68b-4292-8b0b-ae0597c5d7d8");
            orderRequest.put("continueUrl", successUrl + "?orderId=" + order.getId());
            orderRequest.put("customerIp", "127.0.0.1");

            List<Map<String, Object>> products = new ArrayList<>();
            Map<String, Object> product = new HashMap<>();
            product.put("name", "Order #" + order.getId());
            product.put("unitPrice", toPayuAmount(amountPln));
            product.put("quantity", 1);
            products.add(product);

            orderRequest.put("products", products);
            orderRequest.put("totalAmount", toPayuAmount(amountPln));
            orderRequest.put("currencyCode", "PLN");
            orderRequest.put("description", "Order #" + order.getId());
            orderRequest.put("merchantPosId", this.posId);
            orderRequest.put("extOrderId", "ORDER_" + order.getId() + "_" + System.currentTimeMillis());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderRequest, headers);

            String paymentUrl = this.apiUrl + "/api/v2_1/orders";

            ResponseEntity<Map> response = restTemplate.postForEntity(paymentUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.FOUND || response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody != null) {
                    String redirectUri = (String) responseBody.get("redirectUri");
                    String payuOrderId = (String) responseBody.get("orderId");

                    if (redirectUri != null) {
                        log.info("PayU payment created successfully. PayU OrderId: {}, Redirect: {}", payuOrderId, redirectUri);
                        return redirectUri;
                    }
                }

                throw new RuntimeException("Invalid response from PayU: " + responseBody);
            } else {
                throw new RuntimeException("PayU API returned status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to create PayU payment for order: {}", order.getId(), e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    private String toPayuAmount(Double amount) {
        return String.valueOf(Math.round(amount * 100 * 4));//USD
    }

    @Transactional
    public void handleWebhookEvent(String payload, String signature) throws Exception {
        try {
            PayUNotification notification = objectMapper.readValue(payload, PayUNotification.class);

            if ("correct".equals(notification.getStatus()) || "success".equals(notification.getStatus())) {
                handleSuccessfulPayment(notification);
            } else if ("pending".equals(notification.getStatus())) {
                handlePendingPayment(notification);
            } else if ("error".equals(notification.getStatus()) || "failed".equals(notification.getStatus())) {
                handleFailedPayment(notification);
            } else {
                log.warn("Unknown TPay payment status: {}", notification.getStatus());
            }

        } catch (Exception e) {
            log.error("TPay webhook processing failed", e);
            throw e;
        }
    }

    private void handleSuccessfulPayment(PayUNotification notification) throws InsufficientResourcesException {
        try {
            Integer orderId = extractOrderIdFromCrc(notification.getCrc());

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setShopOrderId(orderId);
            paymentRequest.setTransactionId(notification.getId());
            paymentRequest.setPaymentIntentId(notification.getId());
            paymentRequest.setProvider("TPay");
            paymentRequest.setStatus(Payment.PaymentStatus.SUCCEEDED);
            paymentRequest.setPaymentDate(LocalDateTime.now());

            paymentService.createPayment(paymentRequest);

            log.info("TPay payment successful for order: {}, transaction: {}", orderId, notification.getId());

        } catch (Exception e) {
            log.error("Failed to process successful TPay payment", e);
            throw e;
        }
    }

    private void handlePendingPayment(PayUNotification notification) {
        log.info("TPay payment pending for transaction: {}", notification.getId());
    }

    private void handleFailedPayment(PayUNotification notification) {
        log.warn("TPay payment failed for transaction: {}, error: {}",
                notification.getId(), notification.getError());
    }

    private Integer extractOrderIdFromCrc(String crc) {
        if (crc != null && crc.startsWith("ORDER_")) {
            return Integer.parseInt(crc.substring(6));
        }
        throw new IllegalArgumentException("Invalid CRC format: " + crc);
    }

    private String generateOrderDescription(ShopOrderResponse order) {
        StringBuilder description = new StringBuilder("Order #" + order.getId() + " - Products: ");

        for (OrderLineResponse line : order.getOrderLines()) {
            description.append(line.getProductName())
                    .append(" (x")
                    .append(line.getQty())
                    .append("), ");
        }

        if (description.length() > 2) {
            description.setLength(description.length() - 2);
        }

        return description.toString();
    }

    private void validateStock(ShopOrderResponse order) throws InsufficientResourcesException {
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
                Integer stock = stockMap.get(productItem.getId());
                int requestedQty = orderLine.getQty();

                productItem.setQtyInStock(stock != null ? stock : 0);

                if (productItem.getQtyInStock() < requestedQty) {
                    throw new InsufficientResourcesException("Product " + productItem.getId() + " has insufficient stock");
                }
            }
        }
    }


}
