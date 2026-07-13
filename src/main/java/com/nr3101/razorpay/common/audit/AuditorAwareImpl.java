package com.nr3101.razorpay.common.audit;

import com.nr3101.razorpay.merchant.security.MerchantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This class is used to get the current auditor (user) for auditing purposes. It implements the AuditorAware interface from Spring Data JPA.
 * The getCurrentAuditor() method retrieves the current auditor based on the context of the request. It first checks if a keyId is present in the MerchantContext, and if so, returns it as the current auditor. If not, it checks if a merchantId is present and returns it as the current auditor. If neither is present, it defaults to returning "SYSTEM" as the current auditor.
 */
@Component("auditorAwareImpl")
@RequiredArgsConstructor
@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    private final MerchantContext merchantContext;

    @Override
    public Optional<String> getCurrentAuditor() {

        try {
            String keyId = merchantContext.getKeyId();
            if (keyId != null && !keyId.isBlank()) return Optional.of(keyId);

            if (merchantContext.getMerchantId() != null) {
                return Optional.of("merchant_id: " + merchantContext.getMerchantId());
            }
        } catch (Exception e) {
            log.warn("Failed to get current auditor from MerchantContext: {}", e.getMessage());
        }

        return Optional.of("SYSTEM");
    }
}
