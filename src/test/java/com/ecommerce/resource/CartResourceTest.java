package com.ecommerce.resource;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.service.CartService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartResourceTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartResource cartResource;

    private Cart testCart;
    private String userId = "test-user";

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(userId);
        testCart.setTotalAmount(BigDecimal.ZERO);
    }

    @Test
    void getCart_ExistingCart_ReturnsCart() {
        when(cartService.getCart(userId)).thenReturn(Uni.createFrom().item(testCart));

        Response response = cartResource.getCart(userId)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testCart, response.getEntity());
        verify(cartService).getCart(userId);
    }

    @Test
    void addToCart_ValidProduct_AddsToCart() {
        when(cartService.addToCart(userId, 1L, 2)).thenReturn(Uni.createFrom().item(testCart));

        Response response = cartResource.addToCart(userId, 1L, 2)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testCart, response.getEntity());
        verify(cartService).addToCart(userId, 1L, 2);
    }
}