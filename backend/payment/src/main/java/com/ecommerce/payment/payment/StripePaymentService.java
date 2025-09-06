package com.ecommerce.payment.payment;

import com.ecommerce.payment.clients.ProductItemCallerService;
import com.ecommerce.payment.clients.ShopOrderCallerService;
import com.ecommerce.payment.clients.UserClient;
import com.ecommerce.payment.clients.dto.*;
import com.ecommerce.payment.paymentType.PaymentTypeRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InsufficientResourcesException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentService paymentService;
    private final ProductItemCallerService productItemCallerService;
    private final ShopOrderCallerService shopOrderCallerService;

    @Value("${application.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    public String createCheckoutSession(ShopOrderResponse order, String successUrl, String cancelUrl)
            throws StripeException, InsufficientResourcesException {

        validateStock(order);

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .putMetadata("order_id", order.getId().toString())
                .putMetadata("user_id", order.getUserId());

        for (OrderLineResponse line : order.getOrderLines()) {

            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(Long.valueOf(line.getQty()))
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount((long) (line.getPrice() * 100))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(line.getProductName() + " x" + line.getQty())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        if (order.getShippingMethod() != null && order.getShippingMethod().name != null && order.getShippingMethod().getPrice() > 0) {
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount((long) (order.getShippingMethod().getPrice() * 100))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName("Shipping: " + order.getShippingMethod().getName())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }


        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }

    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) throws StripeException, InsufficientResourcesException {

        String webhookSecret = stripeWebhookSecret;

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();
                handleSuccessfulPayment(session);
            }
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature! Check webhook secret");
            throw e;
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            throw e;
        }
    }

    private void handleSuccessfulPayment(Session session) throws StripeException, InsufficientResourcesException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
        Integer orderId = Integer.parseInt(session.getMetadata().get("order_id"));

        String cardBrand = paymentMethod.getCard().getBrand();
        String last4 = paymentMethod.getCard().getLast4();

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setShopOrderId(orderId);
        paymentRequest.setTransactionId(session.getId());
        paymentRequest.setPaymentIntentId(paymentIntent.getId());
        paymentRequest.setLast4CardNumber(last4);
        paymentRequest.setProvider(cardBrand);
        paymentRequest.setStatus(Payment.PaymentStatus.SUCCEEDED);

        paymentService.createPayment(paymentRequest);
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
