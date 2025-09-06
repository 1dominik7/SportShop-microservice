package com.ecommerce.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceNotFoundException extends APIException {
    public ServiceNotFoundException(String service, String function, String message) {
        super("Cannot retrieve data from " + service + " service, during " + function + ". Error: " + message);
    }
}
