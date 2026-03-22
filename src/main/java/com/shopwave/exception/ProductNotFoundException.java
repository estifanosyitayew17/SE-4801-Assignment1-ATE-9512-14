// ID: ATE/9512/14
package com.shopwave.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(Long id) {
        super(String.format("Product with ID %d not found", id));
    }

    public ProductNotFoundException(String name, String value) {
        super(String.format("Product with %s '%s' not found", name, value));
    }
}