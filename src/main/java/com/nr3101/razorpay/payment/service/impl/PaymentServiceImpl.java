package com.nr3101.razorpay.payment.service.impl;

import com.nr3101.razorpay.common.enums.OrderStatus;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;

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
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);


        return paymentMapper.toResponse(payment);
    }
}
