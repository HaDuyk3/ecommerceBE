package com.haduy.ecommerce.payment.repository;

import com.haduy.ecommerce.common.enums.PaymentProvider;
import com.haduy.ecommerce.common.enums.PaymentStatus;
import com.haduy.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByProviderRef(String providerRef);

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status IN :statuses")
    Optional<Payment> findActiveByOrderId(@Param("orderId") UUID orderId,
                                          @Param("statuses") List<PaymentStatus> statuses);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt < :cutoff")
    List<Payment> findByStatusAndCreatedAtBefore(@Param("status") PaymentStatus status,
                                                 @Param("cutoff") java.time.Instant cutoff);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.provider = :provider AND p.status = 'SUCCESS' " +
           "AND p.paidAt >= :from AND p.paidAt < :to")
    long countSuccessByProviderAndPeriod(@Param("provider") PaymentProvider provider,
                                         @Param("from") java.time.Instant from,
                                         @Param("to") java.time.Instant to);
}