package com.haduy.ecommerce.order.repository;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.id = :itemId AND o.user.id = :userId")
    Optional<Order> findByItemIdAndUserId(@Param("itemId") UUID itemId, @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :cutoff")
    List<Order> findByStatusAndCreatedAtBefore(@Param("status") OrderStatus status,
                                               @Param("cutoff") Instant cutoff);
}
