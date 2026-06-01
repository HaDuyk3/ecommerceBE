package com.haduy.ecommerce.review.entity;

import com.haduy.ecommerce.common.entity.BaseEntity;
import com.haduy.ecommerce.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Cache lại để query nhanh
    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID userId;
}