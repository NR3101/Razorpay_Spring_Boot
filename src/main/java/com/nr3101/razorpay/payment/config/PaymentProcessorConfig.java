package com.nr3101.razorpay.payment.config;

import com.nr3101.razorpay.common.enums.PaymentMethod;
import com.nr3101.razorpay.payment.processor.PaymentProcessor;
import com.nr3101.razorpay.payment.processor.strategy.CardPaymentProcessor;
import com.nr3101.razorpay.payment.processor.strategy.NetBankingPaymentProcessor;
import com.nr3101.razorpay.payment.processor.strategy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap() {
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.NETBANKING, netBankingPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor
        );
    }
}
