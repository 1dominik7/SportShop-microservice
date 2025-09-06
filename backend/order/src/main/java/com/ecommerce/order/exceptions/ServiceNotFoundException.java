package com.ecommerce.order.exceptions;

public class ServiceNotFoundException extends APIException {
    public ServiceNotFoundException(String service, String function, String message) {
        super("Cannot retrieve data from " + service + " service, during " + function + ". Error: " + message);
    }
}
