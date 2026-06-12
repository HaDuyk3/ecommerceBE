package com.haduy.ecommerce.payment.job;

import com.haduy.ecommerce.common.enums.PaymentProvider;
import com.haduy.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Compares our SUCCESS payment count against what the gateway should have charged.
 * In production, replace gatewayChargeCount() with the actual gateway reconciliation API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationJob {

    private final PaymentRepository paymentRepository;

    // Runs daily at 02:00
    @Scheduled(cron = "${app.payment.reconciliation-cron:0 0 2 * * *}")
    public void reconcile() {
        Instant to = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant from = to.minus(24, ChronoUnit.HOURS);

        for (PaymentProvider provider : new PaymentProvider[]{PaymentProvider.VNPAY, PaymentProvider.MOMO}) {
            long dbCount = paymentRepository.countSuccessByProviderAndPeriod(provider, from, to);
            long gatewayCount = queryGatewayChargeCount(provider, from, to);

            if (gatewayCount < 0) {
                log.info("Reconciliation [{}]: gateway query not available, DB count = {}", provider, dbCount);
                continue;
            }

            if (dbCount != gatewayCount) {
                log.error("RECONCILIATION MISMATCH [{}] period {}/{}:  DB={} GATEWAY={} — manual review required",
                        provider, from, to, dbCount, gatewayCount);
            } else {
                log.info("Reconciliation [{}]: OK ({} payments)", provider, dbCount);
            }
        }
    }

    /**
     * Returns the number of successful charges from the gateway for the given period.
     * Returns -1 when the gateway API is not available (sandbox / not integrated).
     */
    private long queryGatewayChargeCount(PaymentProvider provider, Instant from, Instant to) {
        // Real integration: call VNPAY/MoMo settlement API
        return -1;
    }
}
