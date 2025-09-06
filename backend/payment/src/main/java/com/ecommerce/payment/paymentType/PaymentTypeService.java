package com.ecommerce.payment.paymentType;

import com.cloudinary.api.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentTypeService {

    private final PaymentTypeRepository paymentTypeRepository;

    @Transactional
    public PaymentTypeResponse createPaymentType(PaymentTypeRequest request) throws ApiException {

        Optional<PaymentType> existingPaymentType  = paymentTypeRepository.findByValue(request.getValue());

        if(existingPaymentType.isPresent()){
            throw new ApiException("Payment type with this name already exist.");
        }

        PaymentType paymentType = PaymentType.builder()
                .value(request.getValue())
                .build();

        paymentType = paymentTypeRepository.save(paymentType);

           return PaymentTypeResponse.builder()
                   .id(paymentType.getId())
                   .value(request.getValue())
                   .build();
    }

    public List<PaymentTypeResponse> getAllPaymentTypes(){
        return paymentTypeRepository.findAll().stream().map(paymentType -> PaymentTypeResponse.builder()
                .id(paymentType.getId())
                .value(paymentType.getValue()).build()).collect(Collectors.toList());
    }
}
