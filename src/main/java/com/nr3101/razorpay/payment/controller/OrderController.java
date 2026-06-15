package com.nr3101.razorpay.payment.controller;

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

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    UUID merchantId = UUID.fromString("479160e7-75be-499c-a1bb-429dac503a0b");// TODO: Replace with actual merchant ID retrieval logic

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.creatOrder(merchantId,request));
    }
}
