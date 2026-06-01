package com.haduy.ecommerce.offer.entity;

import com.haduy.ecommerce.common.entity.BaseEntity;
import com.haduy.ecommerce.common.enums.SellerProductStatus;
import com.haduy.ecommerce.product.entity.Product;
import com.haduy.ecommerce.seller.entity.Seller;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "seller_products")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal effectivePrice;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal baseShippingFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SellerProductStatus status = SellerProductStatus.ACTIVE;

    @Version
    private Long version;
}