package com.ecommerce.product.exceptions;

public class UnauthorizedException extends APIException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super("Unauthorized access");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
