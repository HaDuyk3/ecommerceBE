package com.haduy.ecommerce.seller.repository;

import com.haduy.ecommerce.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Optional<Seller> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    List<Seller> findTop10ByOrderByRatingDesc();
}