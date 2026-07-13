package com.nr3101.razorpay.payment.statemachine;

import com.nr3101.razorpay.common.enums.PaymentActor;
import com.nr3101.razorpay.common.enums.PaymentEvent;
import com.nr3101.razorpay.common.enums.PaymentStatus;
import com.nr3101.razorpay.payment.entity.Payment;
import com.nr3101.razorpay.payment.entity.PaymentTransitionLog;
import com.nr3101.razorpay.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service responsible for handling payment state transitions and logging the transitions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent event) {
        log.info("Applying event {} to payment {}", event, payment.getId());

        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), event);

        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .toStatus(next)
                .event(event)
                .actor(PaymentActor.SYSTEM)
                .occurredAt(LocalDateTime.now())
                .build();

        payment.setStatus(next);
        paymentTransitionLogRepository.save(log);

        return next;
    }
}
