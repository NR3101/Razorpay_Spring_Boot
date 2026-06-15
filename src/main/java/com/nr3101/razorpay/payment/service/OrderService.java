package com.nr3101.razorpay.payment.service;

import com.nr3101.razorpay.payment.dto.request.CreateOrderRequest;
import com.nr3101.razorpay.payment.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderService {
    OrderResponse creatOrder(UUID merchantId, CreateOrderRequest request);
}
