package com.nr3101.razorpay.payment.service.impl;

import com.nr3101.razorpay.common.enums.OrderStatus;
import com.nr3101.razorpay.common.enums.PaymentEvent;
import com.nr3101.razorpay.common.enums.PaymentStatus;
import com.nr3101.razorpay.common.exception.BusinessRuleViolationException;
import com.nr3101.razorpay.common.exception.ResourceNotFoundException;
import com.nr3101.razorpay.payment.dto.request.PaymentInitRequest;
import com.nr3101.razorpay.payment.dto.response.PaymentResponse;
import com.nr3101.razorpay.payment.entity.OrderRecord;
import com.nr3101.razorpay.payment.entity.Payment;
import com.nr3101.razorpay.payment.gateway.PaymentGatewayRouter;
import com.nr3101.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.nr3101.razorpay.payment.gateway.dto.response.PaymentResult;
import com.nr3101.razorpay.payment.mapper.PaymentMapper;
import com.nr3101.razorpay.payment.repository.OrderRepository;
import com.nr3101.razorpay.payment.repository.PaymentRepository;
import com.nr3101.razorpay.payment.service.PaymentService;
import com.nr3101.razorpay.payment.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequest request) {
        log.info("Initiating payment for merchantId: {}, orderId: {}, method: {}", merchantId, request.orderId(), request.method());

        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.ATTEMPTED) {
            throw new BusinessRuleViolationException("INVALID_ORDER_STATUS", "Order is not in a valid state for payment initiation: " + order.getStatus());
        }

        order.setStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);

        Payment payment = Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .methodDetails(request.methodDetails())
                .build();
        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentId(payment.getId())
                .orderId(order.getId())
                .merchantId(merchantId)
                .amount(order.getAmount())
                .method(request.method())
                .methodDetails(request.methodDetails())
                .build();
        PaymentResult result = paymentGatewayRouter.initiate(paymentRequest);

        switch (result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());

            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }

            case PaymentResult.Success success -> {
                // Result won't be success as our request is async so it will go in pending state after successful payment initiation and will later be marked as success via a callback
                log.warn("Invalid state");
                return null;
            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        // TODO: send an outbox Kafka event

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        log.info("Capturing payment for merchantId: {}, paymentId: {}", merchantId, paymentId);

        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

        if (paymentResult instanceof PaymentResult.Success success) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());

            log.info("Payment captured, paymentId: {}", paymentId);
        } else if (paymentResult instanceof PaymentResult.Failure failure) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());

            log.warn("Payment capture failed, paymentId: {}, errorCode: {}, errorDescription: {}",
                    paymentId, failure.errorCode(), failure.errorDescription());
        }

        payment = paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }
}
