package com.ecommerce.payment.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends APIException {
    public NotFoundException(String fieldName, Optional<String> value) {
        super(buildMessage(fieldName, value));
    }

    private static String buildMessage(String fieldName, Optional<String> value){
        String baseMessage = fieldName + " not found";
        return value.map(v -> baseMessage + " with " + v).orElse(baseMessage);
    }
}
