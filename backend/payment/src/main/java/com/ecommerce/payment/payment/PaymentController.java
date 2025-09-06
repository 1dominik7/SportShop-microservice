package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.ShopOrderCallerService;
import com.ecommerce.payment.clients.dto.ShopOrderResponse;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final ShopOrderCallerService shopOrderCallerService;
    private final StripePaymentService stripePaymentService;

    @PostMapping("/stripe/checkout")
    public ResponseEntity<?> createStripeCheckoutSession(@RequestBody StripeCheckoutRequest request, @AuthenticationPrincipal Jwt jwt) {
        try {
            ShopOrderResponse order = shopOrderCallerService.createShopOrder(request.getOrderRequest(), jwt);

            String checkoutUrl = stripePaymentService.createCheckoutSession(
                    order,
                    request.getSuccessUrl(),
                    request.getCancelUrl()
            );

            return ResponseEntity.ok(Map.of(
                    "checkoutUrl", checkoutUrl,
                    "orderId", order.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            stripePaymentService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid signature");

        } catch (StripeException e) {
            log.error("Stripe processing error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Stripe error: " + e.getMessage());

        } catch (NumberFormatException e) {
            log.error("Invalid metadata format", e);
            return ResponseEntity.badRequest()
                    .body("Invalid order_id or user_id format");

        } catch (Exception e) {
            log.error("Unexpected error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    @GetMapping("/verify/{sessionId}")
    public ResponseEntity<?> verifyPayment(@PathVariable String sessionId, @AuthenticationPrincipal Jwt jwt) {
        try {
            PaymentVerificationResponse response = paymentService.verifyPayment(sessionId, jwt);

            if (response.getStatus() == Payment.PaymentStatus.SUCCEEDED) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during verify payment: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
