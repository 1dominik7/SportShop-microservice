package com.ecommerce.payment.payment;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.payment.clients.ProductItemCallerService;
import com.ecommerce.payment.clients.ShopOrderCallerService;
import com.ecommerce.payment.clients.dto.*;
import com.ecommerce.payment.exceptions.APIException;
import com.ecommerce.payment.kafka.KafkaProducers;
import com.ecommerce.payment.paymentType.PaymentTypeRepository;
import com.ecommerce.payment.paymentType.PaymentTypeService;
import com.ecommerce.payment.rabbitMq.OrderEmailProducer;
import com.stripe.model.Card;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import javax.naming.InsufficientResourcesException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private Jwt jwt;

    @Mock
    private Session session;

    @Mock
    private PaymentIntent paymentIntent;

    @Mock
    private PaymentMethod paymentMethod;

    @Mock
    private Payment payment;

    @Mock
    private ShopOrderResponse shopOrderResponse;

    @Mock
    private OrderEmailProducer orderEmailProducer;

    @Mock
    private ShopOrderCallerService shopOrderCallerService;

    @InjectMocks
    private StripePaymentService stripePaymentService;

    @Test
    void PaymentService_CreatePayment_Success() throws InsufficientResourcesException {

        PaymentRequest request = PaymentRequest.builder()
                .transactionId("tx123")
                .paymentIntentId("pi_456")
                .shopOrderId(10)
                .provider("Visa")
                .last4CardNumber("1234")
                .paymentDate(LocalDateTime.now())
                .status(Payment.PaymentStatus.SUCCEEDED)
                .build();

        Payment payment = Payment.builder()
                .id(1)
                .transactionId("tx123")
                .paymentIntentId("pi_456")
                .shopOrderId(10)
                .provider("Visa")
                .last4CardNumber("1234")
                .paymentDate(request.getPaymentDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Payment.PaymentStatus.SUCCEEDED)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.createPayment(request);

        assertNotNull(result);
        assertEquals(payment.getId(), result.getId());
        assertEquals(request.getTransactionId(), result.getTransactionId());
        assertEquals(request.getPaymentIntentId(), result.getPaymentIntentId());
        assertEquals(request.getShopOrderId(), result.getShopOrderId());
        assertEquals(request.getProvider(), result.getProvider());
        assertEquals(request.getLast4CardNumber(), result.getLast4CardNumber());
        assertEquals(request.getStatus(), result.getStatus());

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void PaymentService_CreatePayment_ShopOrderIdIsNull() {
        PaymentRequest request = PaymentRequest.builder()
                .transactionId("tx123")
                .paymentIntentId("pi_456")
                .shopOrderId(null)
                .status(Payment.PaymentStatus.SUCCEEDED)
                .build();

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void PaymentService_CreatePayment_PaymentNotSucceeded() {
        PaymentRequest request = PaymentRequest.builder()
                .transactionId("tx123")
                .paymentIntentId("pi_456")
                .shopOrderId(null)
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void PaymentService_VerifyPayment_Success() throws Exception  {
        String sessionId = "session_123";
        String paymentIntentId = "pi_456";
        String orderIdStr = "1";
        Integer orderId = 1;
        String userEmail = "user@gmail.com";

        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShopOrderCallerService shopOrderCallerService = mock(ShopOrderCallerService.class);
        ProductItemCallerService productItemCallerService = mock(ProductItemCallerService.class);
        OrderEmailProducer orderEmailProducer = mock(OrderEmailProducer.class);
        KafkaProducers kafkaProducers = mock(KafkaProducers.class);

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                shopOrderCallerService,
                productItemCallerService,
                orderEmailProducer,
                kafkaProducers
        );

        Session session = mock(Session.class);
        Jwt jwt = mock(Jwt.class);
        ShopOrderResponse shopOrderResponse = mock(ShopOrderResponse.class);
        ShopOrderResponse updatedShopOrderResponse = mock(ShopOrderResponse.class);
        Payment payment = mock(Payment.class);
        PaymentIntent paymentIntent = mock(PaymentIntent.class);

        when(updatedShopOrderResponse.getPaymentStatus()).thenReturn(Payment.PaymentStatus.SUCCEEDED);
        when(updatedShopOrderResponse.getPaymentId()).thenReturn(1);
        when(updatedShopOrderResponse.getPaymentCreatedAt()).thenReturn(LocalDateTime.now());
       when(updatedShopOrderResponse.getPaymentUpdatedAt()).thenReturn(LocalDateTime.now());

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class);
             MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class);
             MockedStatic<PaymentMethod> mockedPaymentMethod = mockStatic(PaymentMethod.class)) {

            mockedSession.when(() -> Session.retrieve(sessionId)).thenReturn(session);
            when(session.getPaymentIntent()).thenReturn(paymentIntentId);
            when(session.getMetadata()).thenReturn(Map.of("order_id", orderIdStr));

            when(jwt.getClaimAsString("email")).thenReturn(userEmail);

            when(shopOrderCallerService.getUserShopOrderById(orderId, jwt)).thenReturn(shopOrderResponse);

            when(paymentRepository.findByPaymentIntentId(paymentIntentId)).thenReturn(Optional.of(payment));
            when(payment.getPaymentIntentId()).thenReturn(paymentIntentId);

            mockedPaymentIntent.when(() -> PaymentIntent.retrieve(eq(paymentIntentId))).thenReturn(paymentIntent);
            when(paymentIntent.getId()).thenReturn(paymentIntentId);
            when(paymentIntent.getStatus()).thenReturn("succeeded");
            when(paymentIntent.getPaymentMethod()).thenReturn("pm_789");

            when(productItemCallerService.getProductItemsByIds(anyList())).thenReturn(Collections.emptyList());

            PaymentMethod.Card card = mock(PaymentMethod.Card.class);
            when(card.getBrand()).thenReturn("visa");

            PaymentMethod paymentMethod = mock(PaymentMethod.class);
            when(paymentMethod.getCard()).thenReturn(card);

            mockedPaymentMethod.when(() -> PaymentMethod.retrieve("pm_789")).thenReturn(paymentMethod);

            when(updatedShopOrderResponse.getPaymentStatus()).thenReturn(Payment.PaymentStatus.SUCCEEDED);

            when(shopOrderCallerService.updateShopOrder(anyInt(), any(), eq(jwt))).thenReturn(updatedShopOrderResponse);
            doNothing().when(orderEmailProducer).sendOrderEmail(any());

            PaymentVerificationResponse response = paymentService.verifyPayment(sessionId, jwt);

            assertNotNull(response);
            assertEquals(paymentIntentId, response.getPaymentIntentId());
            assertEquals(Payment.PaymentStatus.SUCCEEDED, response.getStatus());

            verify(orderEmailProducer).sendOrderEmail(any());
            verify(shopOrderCallerService).updateShopOrder(anyInt(), any(), eq(jwt));
        }

    }

    @Test
    void PaymentService_UpdateProductStock_Success() throws Exception  {

        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShopOrderCallerService shopOrderCallerService = mock(ShopOrderCallerService.class);
        ProductItemCallerService productItemCallerService = mock(ProductItemCallerService.class);
        OrderEmailProducer orderEmailProducer = mock(OrderEmailProducer.class);
        KafkaProducers kafkaProducers = mock(KafkaProducers.class);

        ProductItemToOrderResponse product1 = mock(ProductItemToOrderResponse.class);
        when(product1.getId()).thenReturn(1);
        when(product1.getQtyInStock()).thenReturn(10);

        OrderLineResponse orderLine1 = mock(OrderLineResponse.class);
        when(orderLine1.getProductItem()).thenReturn(product1);
        when(orderLine1.getQty()).thenReturn(5);

        ShopOrderResponse order = mock(ShopOrderResponse.class);
        when(order.getOrderLines()).thenReturn(List.of(orderLine1));

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                shopOrderCallerService,
                productItemCallerService,
                orderEmailProducer,
                kafkaProducers
        );

        Method method = PaymentService.class.getDeclaredMethod("updateProductStock", ShopOrderResponse.class, Jwt.class);
        method.setAccessible(true);

        Jwt jwt = mock(Jwt.class);

        method.invoke(paymentService, order, jwt);

        ArgumentCaptor<List<ProductStockUpdateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(kafkaProducers).sendUpdateStock(captor.capture());

        List<ProductStockUpdateRequest> updateRequests = captor.getValue();
        assertEquals(1, updateRequests.size());
        assertEquals(1, updateRequests.get(0).getProductItemId());
        assertEquals(5, updateRequests.get(0).getQuantityToSubtract());
    }

    @Test
    void PaymentService_UpdateProductStock_NotEnoughStock() throws Exception  {

        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShopOrderCallerService shopOrderCallerService = mock(ShopOrderCallerService.class);
        ProductItemCallerService productItemCallerService = mock(ProductItemCallerService.class);
        OrderEmailProducer orderEmailProducer = mock(OrderEmailProducer.class);
        KafkaProducers kafkaProducers = mock(KafkaProducers.class);

        ProductItemToOrderResponse product1 = mock(ProductItemToOrderResponse.class);
        when(product1.getId()).thenReturn(1);
        when(product1.getQtyInStock()).thenReturn(2);

        OrderLineResponse orderLine1 = mock(OrderLineResponse.class);
        when(orderLine1.getProductItem()).thenReturn(product1);
        when(orderLine1.getQty()).thenReturn(5);

        ShopOrderResponse order = mock(ShopOrderResponse.class);
        when(order.getOrderLines()).thenReturn(List.of(orderLine1));

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                shopOrderCallerService,
                productItemCallerService,
                orderEmailProducer,
                kafkaProducers
        );

        Method method = PaymentService.class.getDeclaredMethod("updateProductStock", ShopOrderResponse.class, Jwt.class);
        method.setAccessible(true);

        Jwt jwt = mock(Jwt.class);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(paymentService, order, jwt);
        });

        Throwable cause = exception.getCause();
        assertTrue(cause instanceof InsufficientResourcesException);
        assertEquals("Not enough stock for product: 1", cause.getMessage());

        verify(kafkaProducers, never()).sendUpdateStock(any());
    }

    @Test
    void PaymentService_RefreshProductStock_Success() throws Exception {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShopOrderCallerService shopOrderCallerService = mock(ShopOrderCallerService.class);
        ProductItemCallerService productItemCallerService = mock(ProductItemCallerService.class);
        OrderEmailProducer orderEmailProducer = mock(OrderEmailProducer.class);
        KafkaProducers kafkaProducers = mock(KafkaProducers.class);

        ProductItemToOrderResponse product1 = mock(ProductItemToOrderResponse.class);
        when(product1.getId()).thenReturn(1);
        doNothing().when(product1).setQtyInStock(anyInt());

        OrderLineResponse orderLine1 = mock(OrderLineResponse.class);
        when(orderLine1.getProductItem()).thenReturn(product1);

        ShopOrderResponse order = mock(ShopOrderResponse.class);
        when(order.getOrderLines()).thenReturn(List.of(orderLine1));

        ProductItemOneByColourResponse resp = mock(ProductItemOneByColourResponse.class);
        ProductItemOneByColour item = mock(ProductItemOneByColour.class);
        when(item.getId()).thenReturn(1);
        when(item.getQtyInStock()).thenReturn(15);

        when(resp.getProductItemOneByColour()).thenReturn(List.of(item));

        when(productItemCallerService.getProductItemsByIds(List.of(1))).thenReturn(List.of(resp));

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                shopOrderCallerService,
                productItemCallerService,
                orderEmailProducer,
                kafkaProducers
        );
        Method method = PaymentService.class.getDeclaredMethod("refreshProductStock", ShopOrderResponse.class, Jwt.class);
        method.setAccessible(true);

        Jwt jwt = mock(Jwt.class);

        method.invoke(paymentService, order, jwt);

        verify(product1).setQtyInStock(15);
    }
    }
