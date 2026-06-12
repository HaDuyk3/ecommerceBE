package com.haduy.ecommerce.common.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,   // COD: confirmed, awaiting delivery
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}