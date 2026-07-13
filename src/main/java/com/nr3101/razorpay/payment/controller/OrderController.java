package com.nr3101.razorpay.payment.controller;

import com.nr3101.razorpay.merchant.security.MerchantContext;
import com.nr3101.razorpay.payment.dto.request.CreateOrderRequest;
import com.nr3101.razorpay.payment.dto.response.OrderResponse;
import com.nr3101.razorpay.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.creatOrder(merchantContext.getMerchantId(), request));
    }
}
