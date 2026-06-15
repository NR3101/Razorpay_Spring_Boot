package com.nr3101.razorpay.common.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final Object identifier;

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s with identifier '%s' not found", resourceName, identifier));

        this.resourceName = resourceName;
        this.identifier = identifier;
    }
}
