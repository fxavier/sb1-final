package com.ecommerce.service;

import com.ecommerce.domain.dto.ProductWithImagesDTO;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.ProductImage;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

    @Inject
    private ProductRepository productRepository;

    public List<ProductWithImagesDTO> getAllActiveProducts() {
        return productRepository.findAllActiveWithImages()
            .stream()
            .map(this::mapToProductWithImagesDTO)
            .collect(Collectors.toList());
    }

    public List<ProductWithImagesDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findProductsWithImagesByCategory(categoryId)
            .stream()
            .map(this::mapToProductWithImagesDTO)
            .collect(Collectors.toList());
    }

    public ProductWithImagesDTO getProductById(Long id) {
        Product product = productRepository.findProductWithImagesById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToProductWithImagesDTO(product);
    }

    public List<ProductWithImagesDTO> searchProductsByName(String name) {
        return productRepository.findProductsWithImagesByName(name)
            .stream()
            .map(this::mapToProductWithImagesDTO)
            .collect(Collectors.toList());
    }

    public Page<ProductWithImagesDTO> getProductsWithCoverImage(Pageable pageable) {
        return productRepository.findProductsWithCoverImage(pageable)
            .map(this::mapToProductWithImagesDTO);
    }

    public ProductWithImagesDTO getProductDetails(Long id) {
        Product product = productRepository.findProductDetailsWithImages(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToProductWithImagesDTO(product);
    }

    private ProductWithImagesDTO mapToProductWithImagesDTO(Product product) {
        ProductWithImagesDTO dto = new ProductWithImagesDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setAverageRating(product.getAverageRating());
        dto.setActive(product.getActive());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Set cover image
        product.getImages().stream()
            .filter(ProductImage::getIsCover)
            .findFirst()
            .ifPresent(coverImage -> dto.setCoverImageUrl(coverImage.getImageUrl()));

        // Set all image URLs
        List<String> imageUrls = product.getImages().stream()
            .map(ProductImage::getImageUrl)
            .collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        return dto;
    }
}