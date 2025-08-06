package com.example.store.exeption;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer with id " + id + " not found");
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}