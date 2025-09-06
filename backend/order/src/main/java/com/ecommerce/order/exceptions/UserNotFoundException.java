package com.ecommerce.order.exceptions;

public class UserNotFoundException extends APIException {
    public UserNotFoundException(String fieldName, String value) {
        super("User not found with " + fieldName + ": " + value);
    }
}
