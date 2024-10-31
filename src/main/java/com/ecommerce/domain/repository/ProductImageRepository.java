package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProductId(Long productId);
    
    Optional<ProductImage> findByProductIdAndIsCoverTrue(Long productId);
    
    @Modifying
    @Query("UPDATE ProductImage p SET p.isCover = false WHERE p.product.id = :productId AND p.id != :imageId")
    void unsetOtherCoverImages(Long productId, Long imageId);
    
    @Query("SELECT p.imageUrl FROM ProductImage p WHERE p.product.id = :productId")
    List<String> findImageUrlsByProductId(Long productId);
    
    void deleteByProductId(Long productId);
}