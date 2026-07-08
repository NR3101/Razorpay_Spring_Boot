package com.nr3101.razorpay.payment.processor.strategy;

import com.nr3101.razorpay.common.util.RandomizerUtil;
import com.nr3101.razorpay.payment.processor.PaymentProcessor;
import com.nr3101.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.nr3101.razorpay.payment.processor.dto.response.PaymentProcessorResponse;

public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okbank";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("vpa").toString() : null;

        // Simulate the failure
        if (VPA_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("UPI_REJECTED", "Bank rejected the transaction registration");
        }

        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);


        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
