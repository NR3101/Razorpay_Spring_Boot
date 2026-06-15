package com.nr3101.razorpay.payment.repository;

import com.nr3101.razorpay.payment.entity.Payment;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface PaymentRepository extends ListCrudRepository<Payment, UUID> {
}