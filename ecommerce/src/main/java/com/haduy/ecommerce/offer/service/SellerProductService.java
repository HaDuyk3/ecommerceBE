package com.haduy.ecommerce.offer.service;

import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.dto.SellerProductDto;
import com.haduy.ecommerce.offer.dto.SellerProductRequest;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.offer.repository.SellerProductRepository;
import com.haduy.ecommerce.common.enums.ProductStatus;
import com.haduy.ecommerce.product.entity.Product;
import com.haduy.ecommerce.product.service.ProductService;
import com.haduy.ecommerce.seller.entity.Seller;
import com.haduy.ecommerce.seller.service.SellerService;
import com.haduy.ecommerce.offer.dto.SellerProductSearchCriteria;
import com.haduy.ecommerce.offer.spec.SellerProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

        if (product.getStatus() == ProductStatus.PENDING) {
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

    public Page<SellerProductDto> searchMyListings(UUID userId,
                                                   SellerProductSearchCriteria criteria,
                                                   Pageable pageable) {
        Seller seller = sellerService.findByUserIdOrThrow(userId);
        SellerProductSearchCriteria effective = SellerProductSearchCriteria.builder()
                .sellerId(seller.getId())
                .productId(criteria.getProductId())
                .status(criteria.getStatus())
                .inStock(criteria.getInStock())
                .build();
        Specification<SellerProduct> spec = SellerProductSpecifications.from(effective);
        return sellerProductRepository.findAll(spec, pageable).map(SellerProductDto::from);
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

    @Transactional
    public void updateEffectivePrice(UUID sellerProductId, BigDecimal effectivePrice) {
        SellerProduct sp = findOrThrow(sellerProductId);
        sp.setEffectivePrice(effectivePrice);
        sellerProductRepository.save(sp);
        updateLowestPrice(sp.getProduct().getId());
    }
}