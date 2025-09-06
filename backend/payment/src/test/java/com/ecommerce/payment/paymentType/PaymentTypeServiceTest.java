package com.ecommerce.payment.paymentType;

import com.cloudinary.api.exceptions.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PaymentTypeServiceTest {

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @InjectMocks
    private PaymentTypeService paymentTypeService;

    @Mock
    private Jwt jwt;

    @Test
    void PaymentTypeService_CreatePaymentType_Success() throws ApiException {

        PaymentTypeRequest paymentTypeRequest = PaymentTypeRequest.builder()
                .value("Visa")
                .build();

        PaymentType paymentType = PaymentType.builder()
                .id(1)
                .value("Visa")
                .build();

        when(paymentTypeRepository.findByValue(paymentTypeRequest.getValue())).thenReturn(Optional.empty());
        when(paymentTypeRepository.save(any(PaymentType.class))).thenReturn(paymentType);

        PaymentTypeResponse result = paymentTypeService.createPaymentType(paymentTypeRequest);

        assertNotNull(result);
        assertEquals(paymentTypeRequest.getValue(), result.getValue());

        verify(paymentTypeRepository).findByValue(paymentTypeRequest.getValue());
        verify(paymentTypeRepository).save(any(PaymentType.class));
    }

    @Test
    void PaymentTypeService_CreatePaymentType_ValueAlreadyExists() throws ApiException {

        PaymentTypeRequest paymentTypeRequest = PaymentTypeRequest.builder()
                .value("Visa")
                .build();

        PaymentType paymentType = PaymentType.builder()
                .id(1)
                .value("Visa")
                .build();

        when(paymentTypeRepository.findByValue("Visa")).thenReturn(Optional.of(paymentType));

        ApiException exception = assertThrows(ApiException.class, () ->
                paymentTypeService.createPaymentType(paymentTypeRequest)
        );

        assertEquals("Payment type with this name already exist.", exception.getMessage());

        verify(paymentTypeRepository).findByValue("Visa");
        verify(paymentTypeRepository, never()).save(any(PaymentType.class));
    }

    @Test
    void PaymentTypeService_GetAllPaymentTypes_Success() throws ApiException {

        PaymentType paymentType1 = PaymentType.builder()
                .id(1)
                .value("Visa")
                .build();

        PaymentType paymentType2 = PaymentType.builder()
                .id(2)
                .value("Mastercard")
                .build();

        List<PaymentType> paymentTypes = List.of(paymentType1,paymentType2);

        when(paymentTypeRepository.findAll()).thenReturn(paymentTypes);
        List<PaymentTypeResponse> result = paymentTypeService.getAllPaymentTypes();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1, result.get(0).getId());
        assertEquals("Visa", result.get(0).getValue());

        assertEquals(2, result.get(1).getId());
        assertEquals("Mastercard", result.get(1).getValue());

        verify(paymentTypeRepository).findAll();
    }
}
