package com.haduy.ecommerce.payment.job;

import com.haduy.ecommerce.common.enums.PaymentStatus;
import com.haduy.ecommerce.payment.entity.Payment;
import com.haduy.ecommerce.payment.repository.PaymentRepository;
import com.haduy.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTimeoutJob {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Value("${app.payment.processing-timeout-minutes:30}")
    private int timeoutMinutes;

    @Scheduled(fixedDelayString = "${app.payment.timeout-check-interval-ms:300000}")
    @Transactional
    public void expireStuckPayments() {
        Instant cutoff = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        List<Payment> stuck = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.PROCESSING, cutoff);

        if (stuck.isEmpty()) return;
        log.info("PaymentTimeoutJob: found {} stuck PROCESSING payments", stuck.size());

        for (Payment payment : stuck) {
            try {
                // Ask gateway for the real status first
                String resolvedStatus = paymentService.queryGatewayStatus(payment)
                        .orElse("EXPIRED");

                payment.setStatus(switch (resolvedStatus.toUpperCase()) {
                    case "SUCCESS" -> {
                        payment.setPaidAt(Instant.now());
                        yield PaymentStatus.SUCCESS;
                    }
                    case "FAILED" -> PaymentStatus.FAILED;
                    default -> PaymentStatus.EXPIRED;
                });

                paymentRepository.save(payment);
                paymentService.releaseInflightLock(payment.getOrder().getId());

                log.info("Timed-out payment {} resolved to {}", payment.getId(), payment.getStatus());
            } catch (Exception e) {
                log.error("Failed to resolve timed-out payment {}: {}", payment.getId(), e.getMessage());
            }
        }
    }
}
