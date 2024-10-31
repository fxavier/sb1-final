package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images img " +
           "WHERE p.active = true " +
           "ORDER BY CASE WHEN img.isCover = true THEN 0 ELSE 1 END")
    List<Product> findAllActiveWithImages();
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images img " +
           "WHERE p.category.id = :categoryId AND p.active = true " +
           "ORDER BY CASE WHEN img.isCover = true THEN 0 ELSE 1 END")
    List<Product> findProductsWithImagesByCategory(Long categoryId);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images img " +
           "WHERE p.id = :id AND p.active = true")
    Optional<Product> findProductWithImagesById(Long id);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images img " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND p.active = true " +
           "ORDER BY CASE WHEN img.isCover = true THEN 0 ELSE 1 END")
    List<Product> findProductsWithImagesByName(String name);
    
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.images img " +
           "WHERE p.active = true " +
           "AND img.isCover = true")
    Page<Product> findProductsWithCoverImage(Pageable pageable);
    
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.id = :id")
    Optional<Product> findProductDetailsWithImages(Long id);
}