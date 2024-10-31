package com.ecommerce.resource;

import com.ecommerce.domain.dto.ProductDTO;
import com.ecommerce.domain.model.Product;
import com.ecommerce.service.ProductService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductResourceTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductResource productResource;

    private Product testProduct;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(10);
        testProduct.setActive(true);

        productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setStockQuantity(10);
    }

    @Test
    void getProducts_ReturnsProductsList() {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getFilteredProducts(any())).thenReturn(Uni.createFrom().item(products));

        Response response = productResource.getProducts(null, null, null, null, null, null, null, null, null, null)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(productService).getFilteredProducts(any());
    }

    @Test
    void getProduct_ExistingProduct_ReturnsProduct() {
        when(productService.getProductById(1L)).thenReturn(Uni.createFrom().item(testProduct));

        Response response = productResource.getProduct(1L)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testProduct, response.getEntity());
        verify(productService).getProductById(1L);
    }

    @Test
    void createProduct_ValidProduct_CreatesProduct() {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(Uni.createFrom().item(testProduct));

        Response response = productResource.createProduct(productDTO)
            .await().indefinitely();

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testProduct, response.getEntity());
        verify(productService).createProduct(productDTO);
    }

    @Test
    void updateProduct_ExistingProduct_UpdatesProduct() {
        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(Uni.createFrom().item(testProduct));

        Response response = productResource.updateProduct(1L, productDTO)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testProduct, response.getEntity());
        verify(productService).updateProduct(eq(1L), any(ProductDTO.class));
    }

    @Test
    void deleteProduct_ExistingProduct_DeletesProduct() {
        when(productService.deleteProduct(1L)).thenReturn(Uni.createFrom().item(true));

        Response response = productResource.deleteProduct(1L)
            .await().indefinitely();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(productService).deleteProduct(1L);
    }
}