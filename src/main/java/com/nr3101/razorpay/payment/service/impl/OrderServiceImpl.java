package com.nr3101.razorpay.payment.service.impl;

import com.nr3101.razorpay.common.enums.OrderStatus;
import com.nr3101.razorpay.common.exception.DuplicateResourceException;
import com.nr3101.razorpay.payment.dto.request.CreateOrderRequest;
import com.nr3101.razorpay.payment.dto.response.OrderResponse;
import com.nr3101.razorpay.payment.entity.OrderRecord;
import com.nr3101.razorpay.payment.repository.OrderRepository;
import com.nr3101.razorpay.payment.service.OrderService;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Value("${payment.order.default-order-expiration-minutes:30}")
    private int DEFAULT_ORDER_EXPIRATION_MINUTES;

    @Override
    public OrderResponse creatOrder(UUID merchantId, CreateOrderRequest request) {
        log.info("Creating order for merchantId: {} with request: {}", merchantId, request);

        if(request.receipt()!= null && orderRepository.existsByMerchantIdAndReceipt(merchantId,request.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_ALREADY_EXISTS", "An order with the same receipt already exists for this merchant.");
        }

        OrderRecord order = OrderRecord.builder()
                .merchantId(merchantId)
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .status(OrderStatus.CREATED)
                .expiresAt(request.expiresAt()!= null ? request.expiresAt() : LocalDateTime.now().plusMinutes(DEFAULT_ORDER_EXPIRATION_MINUTES))
                .build();
        order = orderRepository.save(order);

        return OrderResponse.builder()
                .id(order.getId())
                .merchantId(order.getMerchantId())
                .receipt(order.getReceipt())
                .amount(order.getAmount())
                .status(order.getStatus())
                .attempts(order.getAttempts())
                .notes(order.getNotes())
                .expiresAt(order.getExpiresAt())
                .createdAt(null)
                .build();
    }
}
