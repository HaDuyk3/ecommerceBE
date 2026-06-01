package com.haduy.ecommerce.order.repository;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Order> findByStatus(OrderStatus status);
    // OrderRepository.java
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.id = :itemId AND o.user.id = :userId")
    Optional<Order> findByItemIdAndUserId(@Param("itemId") UUID itemId, @Param("userId") UUID userId);
}