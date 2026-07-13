package com.nr3101.razorpay.merchant.security;

import lombok.Data;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@Data
// This is used to tell spring that this been is request scoped
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MerchantContext {

    private UUID merchantId;
    private String keyId;
}
