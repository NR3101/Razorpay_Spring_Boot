package com.nr3101.razorpay.payment.statemachine;

import com.nr3101.razorpay.common.enums.PaymentEvent;
import com.nr3101.razorpay.common.enums.PaymentStatus;
import com.nr3101.razorpay.common.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PaymentStateMachine is responsible for managing the state transitions of a payment based on events.
 * It defines valid transitions between different payment statuses and provides a method to transition
 * from one status to another based on an event.
 */
@Component
public class PaymentStateMachine {

    private record StateTransition(PaymentStatus from, PaymentEvent to) {
    }

    private static final Map<StateTransition, PaymentStatus> VALID_TRANSITIONS = Map.ofEntries(
            Map.entry(new StateTransition(PaymentStatus.CREATED, PaymentEvent.AUTHORIZE_ATTEMPT), PaymentStatus.AUTHORIZING),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_SUCCESS), PaymentStatus.AUTHORIZED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_FAIL), PaymentStatus.FAILED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZED, PaymentEvent.CAPTURE_REQUEST), PaymentStatus.CAPTURING),
            Map.entry(new StateTransition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_SUCCESS), PaymentStatus.CAPTURED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_FAIL), PaymentStatus.FAILED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURED, PaymentEvent.REFUND_INIT), PaymentStatus.PARTIALLY_REFUNDED),
            Map.entry(new StateTransition(PaymentStatus.PARTIALLY_REFUNDED, PaymentEvent.REFUND_COMPLETE), PaymentStatus.REFUNDED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURED, PaymentEvent.REFUND_COMPLETE), PaymentStatus.REFUNDED),
            Map.entry(new StateTransition(PaymentStatus.CAPTURED, PaymentEvent.SETTLE), PaymentStatus.SETTLED),
            Map.entry(new StateTransition(PaymentStatus.SETTLED, PaymentEvent.REFUND_INIT), PaymentStatus.PARTIALLY_REFUNDED),
            Map.entry(new StateTransition(PaymentStatus.CREATED, PaymentEvent.CANCEL), PaymentStatus.CANCELLED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZING, PaymentEvent.CANCEL), PaymentStatus.CANCELLED),
            Map.entry(new StateTransition(PaymentStatus.AUTHORIZED, PaymentEvent.CAPTURE_TIMEOUT), PaymentStatus.AUTH_EXPIRED)
    );

    public PaymentStatus transition(PaymentStatus current, PaymentEvent event) {
        PaymentStatus next = VALID_TRANSITIONS.get(new StateTransition(current, event));
        if (next == null) {
            throw new InvalidStateTransitionException(current.name(), event.name());
        }

        return next;
    }
}
