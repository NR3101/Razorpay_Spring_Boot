package com.nr3101.razorpay.common.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String errorCode;

    public DuplicateResourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
