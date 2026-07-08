package com.nr3101.razorpay.common.exception;

import lombok.Getter;

@Getter
public class InvalidStateTransitionException extends RuntimeException {

    private final String fromState;
    private final String toEvent;

    public InvalidStateTransitionException(String fromState, String toEvent) {
        super("Invalid state transition from " + fromState + " to " + toEvent);
        this.fromState = fromState;
        this.toEvent = toEvent;
    }
}
