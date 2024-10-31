package com.ecommerce.service;

import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.ProductImage;
import com.ecommerce.domain.repository.ProductImageRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ProductImageService productImageService;

    private Product testProduct;
    private ProductImage testImage;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");

        testImage = new ProductImage();
        testImage.setId(1L);
        testImage.setProduct(testProduct);
        testImage.setImageUrl("test-image-url");
        testImage.setIsCover(false);

        testFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    void uploadImage_ValidData_CreatesImage() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(imageStorageService.uploadImage(any(), any(), any(), anyLong()))
            .thenReturn("test-image-url");
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(testImage);

        ProductImage result = productImageService.uploadImage(1L, testFile, false);

        assertNotNull(result);
        assertEquals("test-image-url", result.getImageUrl());
        verify(productImageRepository).save(any(ProductImage.class));
    }

    @Test
    void uploadImage_NonExistingProduct_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productImageService.uploadImage(99L, testFile, false);
        });
        verify(imageStorageService, never()).uploadImage(any(), any(), any(), anyLong());
    }

    @Test
    void deleteImage_ExistingImage_DeletesImage() {
        when(productImageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        productImageService.deleteImage(1L, 1L);

        verify(productImageRepository).delete(testImage);
        verify(imageStorageService).deleteImage("test-image-url");
    }

    @Test
    void setCoverImage_ValidImage_UpdatesCover() {
        when(productImageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(testImage);

        ProductImage result = productImageService.setCoverImage(1L, 1L);

        assertTrue(result.getIsCover());
        verify(productImageRepository).unsetOtherCoverImages(1L, 1L);
        verify(productImageRepository).save(testImage);
    }

    @Test
    void getProductImages_ReturnsImagesList() {
        List<ProductImage> images = Arrays.asList(testImage);
        when(productImageRepository.findByProductId(1L)).thenReturn(images);

        List<ProductImage> result = productImageService.getProductImages(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(productImageRepository).findByProductId(1L);
    }
}