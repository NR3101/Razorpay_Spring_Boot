package com.nr3101.razorpay.payment.service.impl;

import com.nr3101.razorpay.common.enums.OrderStatus;
import com.nr3101.razorpay.common.exception.BusinessRuleViolationException;
import com.nr3101.razorpay.common.exception.DuplicateResourceException;
import com.nr3101.razorpay.common.exception.ResourceNotFoundException;
import com.nr3101.razorpay.payment.dto.request.CreateOrderRequest;
import com.nr3101.razorpay.payment.dto.response.OrderResponse;
import com.nr3101.razorpay.payment.dto.response.PaymentResponse;
import com.nr3101.razorpay.payment.entity.OrderRecord;
import com.nr3101.razorpay.payment.entity.Payment;
import com.nr3101.razorpay.payment.mapper.OrderMapper;
import com.nr3101.razorpay.payment.mapper.PaymentMapper;
import com.nr3101.razorpay.payment.repository.OrderRepository;
import com.nr3101.razorpay.payment.repository.PaymentRepository;
import com.nr3101.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;

    @Value("${payment.order.default-order-expiration-minutes:30}")
    private int DEFAULT_ORDER_EXPIRATION_MINUTES;

    @Override
    @Transactional
    public OrderResponse creatOrder(UUID merchantId, CreateOrderRequest request) {
        log.info("Creating order for merchantId: {} with request: {}", merchantId, request);

        if (request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_ALREADY_EXISTS", "An order with the same receipt already exists for this merchant.");
        }

        OrderRecord order = OrderRecord.builder()
                .merchantId(merchantId)
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() : LocalDateTime.now().plusMinutes(DEFAULT_ORDER_EXPIRATION_MINUTES))
                .build();
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        return orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID) {
            throw new BusinessRuleViolationException("ORDER_CANNOT_BE_CANCELLED", "Order cannot be cancelled as it is already " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        List<Payment> paymentList = paymentRepository.findByOrder_Id(orderId);

        return paymentMapper.toResponseList(paymentList);
    }
}
