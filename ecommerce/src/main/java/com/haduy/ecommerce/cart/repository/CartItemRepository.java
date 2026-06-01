package com.haduy.ecommerce.cart.repository;

import com.haduy.ecommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartIdAndSellerProductId(UUID cartId, UUID sellerProductId);
    void deleteByCartId(UUID cartId);
}