package com.ecommerce.payment.paymentType;

import com.cloudinary.api.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("payment-type")
public class PaymentTypeController {

    private final PaymentTypeService paymentTypeService;

    @PostMapping
    public ResponseEntity<PaymentTypeResponse>  createPaymentType(@RequestBody PaymentTypeRequest request) throws ApiException {
        PaymentTypeResponse paymentTypeResponse = paymentTypeService.createPaymentType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentTypeResponse);
    }


    @GetMapping("/all")
    public ResponseEntity<List<PaymentTypeResponse>> getPaymentTypeResponses() {
        List<PaymentTypeResponse> PaymentTypeResponses = paymentTypeService.getAllPaymentTypes();
        return ResponseEntity.status(HttpStatus.OK).body(PaymentTypeResponses);
    }
}
