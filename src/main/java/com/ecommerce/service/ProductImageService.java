package com.ecommerce.service;

import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.ProductImage;
import com.ecommerce.domain.repository.ProductImageRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductImageService {

    @Inject
    private ProductImageRepository productImageRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private ImageStorageService imageStorageService;

    @Transactional
    public ProductImage uploadImage(Long productId, MultipartFile file, Boolean isCover) throws IOException {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        String imageUrl = imageStorageService.uploadImage(
            file.getOriginalFilename(),
            file.getInputStream(),
            file.getContentType(),
            file.getSize()
        );

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setIsCover(isCover != null && isCover);

        if (image.getIsCover()) {
            productImageRepository.unsetOtherCoverImages(productId, null);
        }

        return productImageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to the specified product");
        }

        String imageUrl = image.getImageUrl();
        productImageRepository.delete(image);
        
        try {
            imageStorageService.deleteImage(imageUrl);
        } catch (Exception e) {
            // Log error but continue with deletion
            e.printStackTrace();
        }
    }

    @Transactional
    public ProductImage setCoverImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to the specified product");
        }

        productImageRepository.unsetOtherCoverImages(productId, imageId);
        image.setIsCover(true);
        return productImageRepository.save(image);
    }

    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    public Optional<ProductImage> getProductCoverImage(Long productId) {
        return productImageRepository.findByProductIdAndIsCoverTrue(productId);
    }

    @Transactional
    public void deleteAllProductImages(Long productId) {
        List<String> imageUrls = productImageRepository.findImageUrlsByProductId(productId);
        productImageRepository.deleteByProductId(productId);
        
        // Delete images from storage asynchronously
        imageUrls.forEach(url -> {
            try {
                imageStorageService.deleteImage(url);
            } catch (Exception e) {
                // Log error but continue with deletion
                e.printStackTrace();
            }
        });
    }
}