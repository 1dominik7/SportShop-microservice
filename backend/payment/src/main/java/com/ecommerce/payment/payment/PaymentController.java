package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.ShopOrderCallerService;
import com.ecommerce.payment.clients.dto.ShopOrderResponse;
import com.ecommerce.payment.kafka.KafkaProducers;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.naming.InsufficientResourcesException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final ShopOrderCallerService shopOrderCallerService;
    private final StripePaymentService stripePaymentService;
    private final PayUPaymentService payUPaymentService;
    private final KafkaProducers kafkaProducers;

    @PostMapping("/stripe/checkout")
    public ResponseEntity<?> createStripeCheckoutSession(@RequestBody CheckoutRequest request, @AuthenticationPrincipal Jwt jwt) throws StripeException, InsufficientResourcesException {
        ShopOrderResponse order = shopOrderCallerService.createShopOrder(request.getOrderRequest(), jwt);

        String checkoutUrl = stripePaymentService.createCheckoutSession(
                order
        );

        return ResponseEntity.ok(Map.of(
                "checkoutUrl", checkoutUrl,
                "orderId", order.getId()
        ));
    }

    @PostMapping("/payu/checkout")
    public ResponseEntity<?> createPayUCheckoutSession(@RequestBody CheckoutRequest request, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) throws InsufficientResourcesException {
       ShopOrderResponse order = shopOrderCallerService.createShopOrder(request.getOrderRequest(), jwt);

        String clientIp = extractClientIp(http);

        String checkoutUrl = payUPaymentService.createCheckoutSession(
                order,
                clientIp
        );

        return ResponseEntity.ok(Map.of(
                "checkoutUrl", checkoutUrl,
                "orderId", order.getId(),
                "provider", "PayU"
        ));
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws StripeException, InsufficientResourcesException {

        stripePaymentService.handleWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/payu/webhook")
    public ResponseEntity<String> handlePayUWebhook(
            @RequestBody String payload,
            @RequestHeader Map<String, String> headers,
            @RequestHeader(value = "Openpayu-Signature", required = false) String signature
    ) throws Exception {
        payUPaymentService.handleWebhookEvent(payload, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stripe/pay-again")
    public ResponseEntity<?> payStripeAgain(@RequestBody PayAgainRequest request, @AuthenticationPrincipal Jwt jwt) throws StripeException, InsufficientResourcesException {

        ShopOrderResponse orderResponse = shopOrderCallerService.getUserShopOrderById(request.getOrderId(), jwt);

        if (orderResponse.getPaymentStatus() == Payment.PaymentStatus.SUCCEEDED) {
            return ResponseEntity.badRequest().body("Order already paid");
        }

        String checkoutUrl = stripePaymentService.createCheckoutSession(orderResponse);

        return ResponseEntity.ok(Map.of(
                "checkoutUrl", checkoutUrl,
                "orderId", orderResponse.getId()
        ));

    }

    @PostMapping("/payu/pay-again")
    public ResponseEntity<?> payUAgain(@RequestBody PayAgainRequest request, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) throws StripeException, InsufficientResourcesException {

        String clientIp = extractClientIp(http);

        ShopOrderResponse orderResponse = shopOrderCallerService.getUserShopOrderById(request.getOrderId(), jwt);

        if (orderResponse.getPaymentStatus() == Payment.PaymentStatus.SUCCEEDED) {
            return ResponseEntity.badRequest().body("Order already paid");
        }

        String checkoutUrl = payUPaymentService.createCheckoutSession(orderResponse, clientIp);

        return ResponseEntity.ok(Map.of(
                "checkoutUrl", checkoutUrl,
                "orderId", orderResponse.getId()
        ));

    }

    @GetMapping("/verify/{sessionId}")
    public ResponseEntity<?> verifyStripePayment(@PathVariable String sessionId, @AuthenticationPrincipal Jwt jwt) throws StripeException, InsufficientResourcesException {
        PaymentVerificationResponse response = paymentService.verifyPayment(sessionId, jwt);

        if (response.getStatus() == Payment.PaymentStatus.SUCCEEDED) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/verify/payu/{orderId}")
    public ResponseEntity<?> verifyPayUPayment(@PathVariable Integer orderId, @AuthenticationPrincipal Jwt jwt) throws StripeException, InsufficientResourcesException {
        PaymentVerificationResponse response = paymentService.verifyPayUPayment(orderId, jwt);

        if (response.getStatus() == Payment.PaymentStatus.SUCCEEDED) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
