package com.nr3101.razorpay.payment.processor.dto.response;

/**
 * Represents the response from a payment processor after initiating a payment.
 * This interface is sealed, i.e. it can only be implemented by the nested classes defined within it.
 * The response can be one of the following types:
 * - {@link Pending}: Indicates that the payment is still pending and requires further action.
 * - {@link Success}: Indicates that the payment was successful and provides relevant references.
 * - {@link Failure}: Indicates that the payment failed and provides error details.
 */
public sealed interface PaymentProcessorResponse permits
        PaymentProcessorResponse.Pending,
        PaymentProcessorResponse.Success,
        PaymentProcessorResponse.Failure {

    record Pending(String processorReference) implements PaymentProcessorResponse {
    }

    record Success(String processorReference, String bankReference) implements PaymentProcessorResponse {
    }

    record Failure(String errorCode, String errorDescription) implements PaymentProcessorResponse {
    }
}
