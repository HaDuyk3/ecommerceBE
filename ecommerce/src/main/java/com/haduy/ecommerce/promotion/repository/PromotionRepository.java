package com.haduy.ecommerce.promotion.repository;

import com.haduy.ecommerce.common.enums.PromotionStatus;
import com.haduy.ecommerce.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    @Query("SELECT p FROM Promotion p WHERE p.sellerProduct.id = :sellerProductId " +
            "AND p.status = 'ACTIVE' " +
            "AND p.startAt <= :now AND p.endAt >= :now")
    Optional<Promotion> findActiveBySellerProductId(
            @Param("sellerProductId") UUID sellerProductId,
            @Param("now") Instant now);

    @Query("SELECT p FROM Promotion p WHERE p.sellerProduct.id = :sellerProductId " +
            "AND p.status = 'ACTIVE' " +
            "AND ((p.startAt <= :endAt AND p.endAt >= :startAt))")
    List<Promotion> findOverlapping(
            @Param("sellerProductId") UUID sellerProductId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    List<Promotion> findBySellerProductSellerUserId(UUID userId);
}