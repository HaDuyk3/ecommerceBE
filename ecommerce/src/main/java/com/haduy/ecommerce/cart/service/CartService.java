package com.haduy.ecommerce.cart.service;

import com.haduy.ecommerce.cart.dto.CartDto;
import com.haduy.ecommerce.cart.dto.CartItemDto;
import com.haduy.ecommerce.cart.dto.CartItemRequest;
import com.haduy.ecommerce.cart.entity.Cart;
import com.haduy.ecommerce.cart.entity.CartItem;
import com.haduy.ecommerce.cart.enums.CartItemStatus;
import com.haduy.ecommerce.cart.repository.CartItemRepository;
import com.haduy.ecommerce.cart.repository.CartRepository;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.offer.service.SellerProductService;
import com.haduy.ecommerce.pricing.dto.PricingResult;
import com.haduy.ecommerce.pricing.service.PricingService;
import com.haduy.ecommerce.user.entity.User;
import com.haduy.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final SellerProductService sellerProductService;
    private final PricingService pricingService;
    private final UserService userService;

    // Read-only: flags price changes but does NOT write to DB (T2.1)
    public CartDto getCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return buildCartDto(cart);
    }

    @Transactional
    public CartDto addItem(UUID userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        User user = userService.findOrThrow(userId);
        SellerProduct sp = sellerProductService.findOrThrow(request.getSellerProductId());

        PricingResult pricing = pricingService.calculate(sp, request.getQuantity(), user);

        cartItemRepository.findByCartIdAndSellerProductId(cart.getId(), sp.getId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setQuantity(existing.getQuantity() + request.getQuantity());
                            snapshotFrom(existing, sp, pricing);
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .sellerProduct(sp)
                                    .quantity(request.getQuantity())
                                    .snapshotPrice(sp.getEffectivePrice())
                                    .snapshotShipping(pricing.getShippingFee())
                                    .snapshotShippingDiscount(pricing.getShippingDiscount())
                                    .build();
                            cartItemRepository.save(item);
                        }
                );

        return buildCartDto(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Transactional
    public CartDto updateQuantity(UUID userId, UUID cartItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        User user = userService.findOrThrow(userId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            PricingResult pricing = pricingService.calculate(
                    item.getSellerProduct(), quantity, user);
            item.setQuantity(quantity);
            snapshotFrom(item, item.getSellerProduct(), pricing);
            cartItemRepository.save(item);
        }

        return buildCartDto(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Transactional
    public void removeItem(UUID userId, UUID cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        cartItemRepository.delete(item);
    }

    // Refreshes snapshot of one item to current live price (T2.2)
    @Transactional
    public CartDto acceptPrice(UUID userId, UUID cartItemId) {
        Cart cart = getOrCreateCart(userId);
        User user = userService.findOrThrow(userId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        PricingResult pricing = pricingService.calculate(
                item.getSellerProduct(), item.getQuantity(), user);
        snapshotFrom(item, item.getSellerProduct(), pricing);
        cartItemRepository.save(item);

        return buildCartDto(cartRepository.findByUserId(userId).orElseThrow());
    }

    public Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userService.findOrThrow(userId);
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    // All totals from snapshot only — no live pricing calls (T2.1 fix)
    private CartDto buildCartDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(CartItemDto::from)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(i -> i.getSnapshotPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = items.stream()
                .map(CartItemDto::getSnapshotShipping)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingDiscount = items.stream()
                .map(CartItemDto::getSnapshotShippingDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(shippingDiscount);

        boolean canCheckout = items.stream()
                .allMatch(i -> i.getStatus() == CartItemStatus.OK);

        return CartDto.builder()
                .cartId(cart.getId())
                .items(items)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .shippingDiscount(shippingDiscount)
                .totalAmount(totalAmount)
                .canCheckout(canCheckout)
                .build();
    }

    private void snapshotFrom(CartItem item, SellerProduct sp, PricingResult pricing) {
        item.setSnapshotPrice(sp.getEffectivePrice());
        item.setSnapshotShipping(pricing.getShippingFee());
        item.setSnapshotShippingDiscount(pricing.getShippingDiscount());
    }
}
