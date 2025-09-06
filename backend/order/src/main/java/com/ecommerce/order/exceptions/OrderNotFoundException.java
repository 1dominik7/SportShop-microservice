package com.ecommerce.order.exceptions;

public class OrderNotFoundException extends APIException {
    public OrderNotFoundException(String fieldName, Object value) {
        super("Order not found with " + fieldName + ": " + value);
    }
}
