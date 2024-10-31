package com.ecommerce.service;

import com.ecommerce.domain.dto.ProductWithImagesDTO;
import com.ecommerce.domain.model.Category;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.ProductImage;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductImage coverImage;
    private ProductImage additionalImage;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(10);
        testProduct.setCategory(category);
        testProduct.setActive(true);

        coverImage = new ProductImage();
        coverImage.setId(1L);
        coverImage.setImageUrl("cover-image-url");
        coverImage.setIsCover(true);
        coverImage.setProduct(testProduct);

        additionalImage = new ProductImage();
        additionalImage.setId(2L);
        additionalImage.setImageUrl("additional-image-url");
        additionalImage.setIsCover(false);
        additionalImage.setProduct(testProduct);

        testProduct.setImages(new HashSet<>(Arrays.asList(coverImage, additionalImage)));
    }

    @Test
    void getAllActiveProducts_ReturnsProductsList() {
        when(productRepository.findAllActiveWithImages())
            .thenReturn(Arrays.asList(testProduct));

        List<ProductWithImagesDTO> result = productService.getAllActiveProducts();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals("cover-image-url", result.get(0).getCoverImageUrl());
        assertEquals(2, result.get(0).getImageUrls().size());
        verify(productRepository).findAllActiveWithImages();
    }

    @Test
    void getProductById_ExistingProduct_ReturnsProduct() {
        when(productRepository.findProductWithImagesById(1L))
            .thenReturn(Optional.of(testProduct));

        ProductWithImagesDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("cover-image-url", result.getCoverImageUrl());
        verify(productRepository).findProductWithImagesById(1L);
    }

    @Test
    void getProductById_NonExistingProduct_ThrowsException() {
        when(productRepository.findProductWithImagesById(99L))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(99L);
        });
        verify(productRepository).findProductWithImagesById(99L);
    }

    @Test
    void searchProductsByName_ReturnsMatchingProducts() {
        when(productRepository.findProductsWithImagesByName("Test"))
            .thenReturn(Arrays.asList(testProduct));

        List<ProductWithImagesDTO> result = productService.searchProductsByName("Test");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository).findProductsWithImagesByName("Test");
    }

    @Test
    void getProductsWithCoverImage_ReturnsPagedProducts() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
        
        when(productRepository.findProductsWithCoverImage(pageRequest))
            .thenReturn(productPage);

        Page<ProductWithImagesDTO> result = productService.getProductsWithCoverImage(pageRequest);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository).findProductsWithCoverImage(pageRequest);
    }
}