package com.nr3101.razorpay.payment.simulator;

import com.nr3101.razorpay.common.enums.ChaosMode;
import com.nr3101.razorpay.common.enums.PaymentStatus;
import com.nr3101.razorpay.common.util.RandomizerUtil;
import com.nr3101.razorpay.payment.entity.Payment;
import com.nr3101.razorpay.payment.repository.PaymentRepository;
import com.nr3101.razorpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This class simulates bank callbacks for payments that are in the AUTHORIZING state. It periodically checks for payments that are due for a callback based on their creation time and the configured delay for their payment method. Depending on the configured chaos mode, it either approves, declines, or simulates a timeout for the payment authorization.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

//    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallbacks() {
        log.info("Processing Bank Callbacks");

        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);

        List<Payment> candidates = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);

        if (candidates.isEmpty()) {
            log.info("No payments found to process");
            return;
        }

        log.info("Found {} payments to process: ", candidates.size());

        for (Payment payment : candidates) {
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {
        SimulatorConfig.MethodSimulatorConfig methodConfig = simulatorConfig.configFor(payment.getMethod());

        LocalDateTime dueAt = dueAt(payment, methodConfig);

        if (LocalDateTime.now().isBefore(dueAt)) {
            log.info("Payment {} is not due for callback yet. Due at: {}", payment.getId(), dueAt);
            return;
        }

        ChaosMode chaosMode = simulatorConfig.getChaosMode();

        switch (chaosMode) {
            case SUCCESS -> resolve(payment, true);
            case FAILURE -> resolve(payment, false);
            case TIMEOUT -> {
                log.info("BankCallbackSimulator timed out for payment: {}", payment.getId());
            }
            case NORMAL, SLOW -> resolve(payment, isApproved(payment, methodConfig));
        }
    }

    public void resolve(Payment payment, boolean approve) {
        if (approve) {
            String banRef = "SIM_BANK_REF" + RandomizerUtil.randomBase64(8);
            paymentService.resolveAuthorization(payment.getId(), true, banRef, null, null);
        } else {
            paymentService.resolveAuthorization(payment.getId(), false, null, "SIM_BANK_ERROR_CODE", "Simulated bank declined");
        }
    }

    private boolean isApproved(Payment payment, SimulatorConfig.MethodSimulatorConfig methodConfig) {
        int bucket = Math.abs(payment.getId().hashCode()) % 100;
        return bucket < methodConfig.getSuccessRate();
    }

    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodConfig) {
        int range = methodConfig.getMaxDelaySeconds() - methodConfig.getMinDelaySeconds();
        int delaySeconds = methodConfig.getMinDelaySeconds() + Math.abs(payment.getId().hashCode()) % (range + 1);

        if (simulatorConfig.getChaosMode() == ChaosMode.SLOW) {
            delaySeconds *= 2;
        }

        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }
}
