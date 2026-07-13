package com.nr3101.razorpay.payment.processor.strategy;

import com.nr3101.razorpay.common.util.RandomizerUtil;
import com.nr3101.razorpay.payment.processor.PaymentProcessor;
import com.nr3101.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.nr3101.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class NetBankingPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("bank").toString() : null;

        // Simulate the failure
        if (BANK_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("BANK_REJECTED", "Bank rejected the transaction registration");
        }

        String processorRef = "NBK_PROCESSOR_" + RandomizerUtil.randomBase64(16);

//        String redirectRef = "http://REDIRECT_BANK.com/" + processorRef;

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
