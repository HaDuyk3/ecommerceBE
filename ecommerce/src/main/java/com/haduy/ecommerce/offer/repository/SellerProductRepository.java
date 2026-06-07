package com.haduy.ecommerce.offer.repository;

import com.haduy.ecommerce.offer.entity.SellerProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SellerProductRepository extends JpaRepository<SellerProduct, UUID>,
        JpaSpecificationExecutor<SellerProduct> {

    List<SellerProduct> findByProductId(UUID productId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SellerProduct sp SET sp.stock = sp.stock - :quantity " +
            "WHERE sp.id = :id AND sp.stock >= :quantity")
    int updateStockConditional(@Param("id") UUID id,
                               @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SellerProduct sp SET sp.stock = sp.stock + :quantity WHERE sp.id = :id")
    int restoreStock(@Param("id") UUID id, @Param("quantity") int quantity);
}
