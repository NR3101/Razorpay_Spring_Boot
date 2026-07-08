package com.nr3101.razorpay.payment.repository;

import com.nr3101.razorpay.payment.entity.PaymentTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentTransactionLogRepository extends JpaRepository<PaymentTransactionLog, UUID> {
}