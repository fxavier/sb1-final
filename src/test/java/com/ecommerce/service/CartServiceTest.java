package com.ecommerce.service;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.CartItem;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private Product testProduct;
    private String userId = "test-user";

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(userId);
        testCart.setTotalAmount(BigDecimal.ZERO);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(10);
    }

    @Test
    void getCart_ExistingCart_ReturnsCart() {
        when(cartRepository.findByUserId(userId)).thenReturn(Uni.createFrom().item(testCart));

        Cart result = cartService.getCart(userId)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void getCart_NonExistingCart_CreatesNewCart() {
        when(cartRepository.findByUserId(userId)).thenReturn(Uni.createFrom().nullItem());
        when(cartRepository.persist(any(Cart.class))).thenReturn(Uni.createFrom().item(testCart));

        Cart result = cartService.getCart(userId)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).persist(any(Cart.class));
    }

    @Test
    void addToCart_ValidProduct_AddsToCart() {
        when(cartRepository.findByUserId(userId)).thenReturn(Uni.createFrom().item(testCart));
        when(productRepository.findById(1L)).thenReturn(Uni.createFrom().item(testProduct));
        when(cartRepository.persist(any(Cart.class))).thenReturn(Uni.createFrom().item(testCart));

        Cart result = cartService.addToCart(userId, 1L, 2)
            .await().indefinitely();

        assertNotNull(result);
        verify(cartRepository).findByUserId(userId);
        verify(productRepository).findById(1L);
        verify(cartRepository).persist(any(Cart.class));
    }

    @Test
    void addToCart_NonExistingProduct_ThrowsException() {
        when(cartRepository.findByUserId(userId)).thenReturn(Uni.createFrom().item(testCart));
        when(productRepository.findById(99L)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart(userId, 99L, 1)
                .await().indefinitely();
        });

        verify(cartRepository).findByUserId(userId);
        verify(productRepository).findById(99L);
        verify(cartRepository, never()).persist(any(Cart.class));
    }

    @Test
    void addToCart_ExistingCartItem_UpdatesQuantity() {
        CartItem existingItem = new CartItem();
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(1);
        existingItem.setPrice(testProduct.getPrice());
        existingItem.setSubtotal(testProduct.getPrice());
        testCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Uni.createFrom().item(testCart));
        when(productRepository.findById(1L)).thenReturn(Uni.createFrom().item(testProduct));
        when(cartRepository.persist(any(Cart.class))).thenReturn(Uni.createFrom().item(testCart));

        Cart result = cartService.addToCart(userId, 1L, 1)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().iterator().next().getQuantity());
        verify(cartRepository).persist(any(Cart.class));
    }
}