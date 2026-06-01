package com.haduy.ecommerce.product.service;

import com.haduy.ecommerce.common.enums.ProductStatus;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.product.dto.ProductDto;
import com.haduy.ecommerce.product.dto.ProductRequest;
import com.haduy.ecommerce.product.entity.Category;
import com.haduy.ecommerce.product.entity.Product;
import com.haduy.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public Page<ProductDto> search(String keyword, UUID categoryId, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return productRepository.searchByKeyword(keyword, ProductStatus.APPROVED, pageable)
                    .map(ProductDto::from);
        }
        if (categoryId != null) {
            return productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.APPROVED, pageable)
                    .map(ProductDto::from);
        }
        return productRepository.findByStatus(ProductStatus.APPROVED, pageable)
                .map(ProductDto::from);
    }

    public ProductDto getById(UUID id) {
        return ProductDto.from(findOrThrow(id));
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Category category = categoryService.findOrThrow(request.getCategoryId());
        Product product = Product.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .description(request.getDescription())
                .category(category)
                .build();
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional
    public ProductDto approve(UUID id) {
        Product product = findOrThrow(id);
        product.setStatus(ProductStatus.APPROVED);
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional
    public void updateLowestPrice(UUID productId, BigDecimal newLowestPrice) {
        Product product = findOrThrow(productId);
        product.setLowestPrice(newLowestPrice);
        productRepository.save(product);
    }

    @Transactional
    public void updateRating(UUID productId, double avgRating, int reviewCount) {
        Product product = findOrThrow(productId);
        product.setAvgRating(avgRating);
        product.setReviewCount(reviewCount);
        productRepository.save(product);
    }

    public Product findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}