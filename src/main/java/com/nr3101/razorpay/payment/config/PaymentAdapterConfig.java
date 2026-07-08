package com.nr3101.razorpay.payment.config;

import com.nr3101.razorpay.common.enums.PaymentMethod;
import com.nr3101.razorpay.payment.gateway.PaymentAdapter;
import com.nr3101.razorpay.payment.gateway.adapter.CardPaymentAdapter;
import com.nr3101.razorpay.payment.gateway.adapter.NetBankingPaymentAdapter;
import com.nr3101.razorpay.payment.gateway.adapter.UpiPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {

    private final NetBankingPaymentAdapter netBankingPaymentAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap() {
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.NETBANKING, netBankingPaymentAdapter,
                PaymentMethod.UPI, upiPaymentAdapter
        );
    }
}
