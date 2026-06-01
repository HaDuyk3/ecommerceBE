package com.haduy.ecommerce.seller.service;

import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.seller.dto.SellerDto;
import com.haduy.ecommerce.seller.dto.SellerRequest;
import com.haduy.ecommerce.seller.entity.Seller;
import com.haduy.ecommerce.seller.repository.SellerRepository;
import com.haduy.ecommerce.user.entity.User;
import com.haduy.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserService userService;

    @Transactional
    public SellerDto register(UUID userId, SellerRequest request) {
        if (sellerRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.SELLER_ALREADY_EXISTS);
        }
        User user = userService.findOrThrow(userId);
        Seller seller = Seller.builder()
                .user(user)
                .shopName(request.getShopName())
                .build();
        return SellerDto.from(sellerRepository.save(seller));
    }

    public SellerDto getMyShop(UUID userId) {
        return SellerDto.from(findByUserIdOrThrow(userId));
    }

    public SellerDto getById(UUID sellerId) {
        return SellerDto.from(sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND)));
    }

    @Transactional
    public SellerDto update(UUID userId, SellerRequest request) {
        Seller seller = findByUserIdOrThrow(userId);
        seller.setShopName(request.getShopName());
        return SellerDto.from(sellerRepository.save(seller));
    }

    @Transactional
    public void updateRating(UUID sellerId, double newRating) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND));
        seller.setRating(newRating);
        sellerRepository.save(seller);
    }

    public Seller findByUserIdOrThrow(UUID userId) {
        return sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND));
    }

    public Seller findOrThrow(UUID sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND));
    }
}