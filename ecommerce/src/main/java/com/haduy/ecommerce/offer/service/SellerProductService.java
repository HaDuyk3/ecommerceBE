package com.haduy.ecommerce.offer.service;

import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.dto.SellerProductDto;
import com.haduy.ecommerce.offer.dto.SellerProductRequest;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.offer.repository.SellerProductRepository;
import com.haduy.ecommerce.product.entity.Product;
import com.haduy.ecommerce.product.service.ProductService;
import com.haduy.ecommerce.seller.entity.Seller;
import com.haduy.ecommerce.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final SellerProductRepository sellerProductRepository;
    private final SellerService sellerService;
    private final ProductService productService;

    @Transactional
    public SellerProductDto create(UUID userId, SellerProductRequest request) {
        Seller seller = sellerService.findByUserIdOrThrow(userId);
        Product product = productService.findOrThrow(request.getProductId());

        if (product.getStatus().name().equals("PENDING")) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_APPROVED);
        }

        SellerProduct sp = SellerProduct.builder()
                .seller(seller)
                .product(product)
                .basePrice(request.getBasePrice())
                .effectivePrice(request.getBasePrice())
                .stock(request.getStock())
                .baseShippingFee(request.getBaseShippingFee())
                .build();

        SellerProduct saved = sellerProductRepository.save(sp);
        updateLowestPrice(product.getId());
        return SellerProductDto.from(saved);
    }

    @Transactional
    public SellerProductDto update(UUID userId, UUID sellerProductId,
                                   SellerProductRequest request) {
        Seller seller = sellerService.findByUserIdOrThrow(userId);
        SellerProduct sp = findOrThrow(sellerProductId);

        if (!sp.getSeller().getId().equals(seller.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        sp.setBasePrice(request.getBasePrice());
        sp.setEffectivePrice(request.getBasePrice());
        sp.setStock(request.getStock());
        sp.setBaseShippingFee(request.getBaseShippingFee());

        SellerProduct saved = sellerProductRepository.save(sp);
        updateLowestPrice(sp.getProduct().getId());
        return SellerProductDto.from(saved);
    }

    public List<SellerProductDto> getByProductId(UUID productId) {
        return sellerProductRepository.findByProductId(productId)
                .stream()
                .map(SellerProductDto::from)
                .toList();
    }

    public List<SellerProductDto> getMyListings(UUID userId) {
        Seller seller = sellerService.findByUserIdOrThrow(userId);
        return sellerProductRepository.findBySellerId(seller.getId())
                .stream()
                .map(SellerProductDto::from)
                .toList();
    }

    public void updateLowestPrice(UUID productId) {
        List<SellerProduct> listings = sellerProductRepository.findByProductId(productId);
        listings.stream()
                .map(SellerProduct::getEffectivePrice)
                .min(Comparator.naturalOrder())
                .ifPresent(lowest -> productService.updateLowestPrice(productId, lowest));
    }

    public SellerProduct findOrThrow(UUID id) {
        return sellerProductRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_PRODUCT_NOT_FOUND));
    }
}