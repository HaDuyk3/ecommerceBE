package com.haduy.ecommerce.order.job;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.order.entity.Order;
import com.haduy.ecommerce.order.repository.OrderRepository;
import com.haduy.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPendingTimeoutJob {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${app.order.pending-timeout-minutes:30}")
    private int timeoutMinutes;

    // Runs every 5 minutes
    @Scheduled(fixedDelayString = "${app.order.timeout-check-interval-ms:300000}")
    public void cancelExpiredPendingOrders() {
        Instant cutoff = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        List<Order> expired = orderRepository
                .findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        if (expired.isEmpty()) return;

        log.info("Timeout job: found {} expired PENDING orders", expired.size());
        for (Order order : expired) {
            try {
                orderService.cancelBySystem(order.getId());
            } catch (Exception e) {
                log.error("Timeout job failed to cancel order {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}
