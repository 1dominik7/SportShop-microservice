package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.ProductItemCallerService;
import com.ecommerce.payment.clients.dto.*;
import com.ecommerce.payment.payment.PayUDTO.PayUOrder;
import com.ecommerce.payment.payment.PayUDTO.PayUPayload;
import com.ecommerce.payment.payment.PayUDTO.PayUProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${application.payu.client-id}")
    private String clientId;

    @Value("${application.payu.client-secret}")
    private String clientSecret;

    @Value("${application.payu.pos-id}")
    private String posId;

    @Value("${application.payu.api-url}")
    private String apiUrl;

    @Value("${application.payu.notify-url}")
    private String notifyUrl;

    @Value("${application.payment.success-url}")
    private String successURL;

    private String getAccessToken() {
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
            return (String) responseBody.get("access_token");
        } else {
            throw new RuntimeException("OAuth failed: " + response.getStatusCode());
        }
    }

    public String createCheckoutSession(ShopOrderResponse order, String clientIp) throws InsufficientResourcesException {

        validateStock(order);

        String accessToken = getAccessToken();

        Map<String, Object> orderRequest = new HashMap<>();

        Double amountUsd = order.getFinalOrderTotal();
        Double amountPln = amountUsd * 4;

        orderRequest.put("notifyUrl", notifyUrl);
        orderRequest.put("continueUrl", successURL + "?order_id=" + order.getId());
        orderRequest.put("customerIp", clientIp);

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
        orderRequest.put("merchantPosId", posId);

        String extOrderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();
        orderRequest.put("extOrderId", extOrderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderRequest, headers);

        String paymentUrl = apiUrl + "/api/v2_1/orders";

        ResponseEntity<Map> response = restTemplate.exchange(
                paymentUrl,
                HttpMethod.POST,
                request,
                Map.class
        );


        if (response.getStatusCode() == HttpStatus.FOUND || response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String redirectUri = (String) responseBody.get("redirectUri");

                if (redirectUri != null) {
                    return redirectUri;
                }
            }

            throw new RuntimeException("Invalid response from PayU: " + responseBody);
        } else {
            throw new RuntimeException("PayU API returned status: " + response.getStatusCode());
        }
    }

    private String toPayuAmount(Double amount) {
        return String.valueOf(Math.round(amount * 100));//USD
    }

    @Transactional
    public void handleWebhookEvent(String payload, String signature) throws Exception {
        PayUPayload payloadObj = objectMapper.readValue(payload, PayUPayload.class);
        PayUOrder order = payloadObj.getOrder();

        if ("COMPLETED".equals(order.getStatus()) || "SUCCESS".equals(order.getStatus())) {
            handleSuccessfulPayment(payloadObj);
        }
    }

    private void handleSuccessfulPayment(PayUPayload payloadObj) throws InsufficientResourcesException {
        Integer orderId = extractOrderIdFromExtOrderId(payloadObj.getOrder().getExtOrderId());

        String transactionId = extractPaymentId(payloadObj.getProperties());
        if (transactionId == null) {
            transactionId = payloadObj.getOrder().getOrderId();
            log.warn("PAYMENT_ID not found, using orderId as transactionId: {}", transactionId);
        }

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setShopOrderId(orderId);
        paymentRequest.setTransactionId(transactionId);
        paymentRequest.setPaymentIntentId(payloadObj.getOrder().getOrderId());
        paymentRequest.setProvider(payloadObj.getOrder().getPayMethod().getType());
        paymentRequest.setStatus(Payment.PaymentStatus.SUCCEEDED);
        paymentRequest.setPaymentDate(LocalDateTime.now());

        paymentService.createPayment(paymentRequest);
    }

    private Integer extractOrderIdFromExtOrderId(String extOrderId) {
        if (extOrderId != null && extOrderId.startsWith("ORDER_")) {
            String[] parts = extOrderId.split("_");
            return Integer.parseInt(parts[1]);
        }
        throw new IllegalArgumentException("Invalid extOrderId: " + extOrderId);
    }

    private String extractPaymentId(List<PayUProperty> properties) {
        if (properties != null) {
            for (PayUProperty prop : properties) {
                if ("PAYMENT_ID".equals(prop.getName())) {
                    return prop.getValue();
                }
            }
        }
        return null;
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
            Integer stock = stockMap.get(orderLine.getProductItem().getId());
            orderLine.setQty(stock != null ? stock : 0);

            if (orderLine.getQty() < orderLine.getQty()) {
                throw new InsufficientResourcesException(
                        "Product " + orderLine.getProductItem().getId() + " has insufficient stock"
                );
            }
        }
    }


}
