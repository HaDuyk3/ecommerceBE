package com.haduy.ecommerce.order.dto;

import com.haduy.ecommerce.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class OrderSearchCriteria {

    private final UUID userId;
    private final UUID orderId;
    private final String userEmail;
    private final OrderStatus status;
    private final Instant fromDate;
    private final Instant toDate;
}
