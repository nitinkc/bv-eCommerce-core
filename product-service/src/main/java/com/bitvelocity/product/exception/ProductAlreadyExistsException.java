package com.bitvelocity.product.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String sku) {
        super("Product already exists with SKU: " + sku);
    }

    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
