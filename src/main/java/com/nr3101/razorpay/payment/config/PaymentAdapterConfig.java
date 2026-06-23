package com.nr3101.razorpay.payment.config;

import com.nr3101.razorpay.common.enums.PaymentMethod;
import com.nr3101.razorpay.payment.gateway.PaymentAdapter;
import com.nr3101.razorpay.payment.gateway.adapter.CardPaymentAdapter;
import com.nr3101.razorpay.payment.gateway.adapter.NetBankingPaymentAdapter;
import com.nr3101.razorpay.payment.gateway.adapter.UpiPaymentAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentAdapterConfig {

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap() {
        return Map.of(
                PaymentMethod.CARD, new CardPaymentAdapter(),
                PaymentMethod.NETBANKING, new NetBankingPaymentAdapter(),
                PaymentMethod.UPI, new UpiPaymentAdapter()
        );
    }
}
