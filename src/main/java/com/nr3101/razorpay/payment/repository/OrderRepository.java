package com.nr3101.razorpay.payment.repository;

import com.nr3101.razorpay.payment.entity.OrderRecord;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderRecord, UUID> {

    boolean existsByMerchantIdAndReceipt(UUID merchantId, String receipt);
}