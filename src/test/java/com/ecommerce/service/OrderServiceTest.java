package com.ecommerce.service;

import com.ecommerce.domain.model.*;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.OrderRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Cart testCart;
    private String userId = "test-user";
    private ShippingAddress shippingAddress;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(userId);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("99.99"));

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(userId);
        testCart.setTotalAmount(new BigDecimal("99.99"));

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("99.99"));

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setPrice(product.getPrice());
        cartItem.setSubtotal(product.getPrice());
        testCart.getItems().add(cartItem);

        shippingAddress = new ShippingAddress();
        shippingAddress.setFullName("John Doe");
        shippingAddress.setAddressLine1("123 Main St");
        shippingAddress.setCity("Test City");
        shippingAddress.setCountry("Test Country");
        shippingAddress.setPostalCode("12345");
    }

    @Test
    void getUserOrders_ReturnsOrdersList() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByUserId(userId)).thenReturn(Uni.createFrom().item(orders));

        List<Order> result = orderService.getUserOrders(userId)
            .await().indefinitely();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void getOrder_ExistingOrder_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Uni.createFrom().item(testOrder));

        Order result = orderService.getOrder(1L)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrder_NonExistingOrder_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrder(99L).await().indefinitely();
        });
        verify(orderRepository).findById(99L);
    }

    @Test
    void createOrder_ValidCart_CreatesOrder() {
        when(cartRepository.findByUserId(userId)).thenReturn(Uni.createFrom().item(testCart));
        when(orderRepository.persist(any(Order.class))).thenReturn(Uni.createFrom().item(testOrder));

        Order result = orderService.createOrder(userId, shippingAddress)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(testCart.getTotalAmount(), result.getTotalAmount());
        verify(cartRepository).findByUserId(userId);
        verify(orderRepository).persist(any(Order.class));
    }

    @Test
    void createOrder_NonExistingCart_ThrowsException() {
        when(cartRepository.findByUserId(userId)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(userId, shippingAddress)
                .await().indefinitely();
        });
        verify(cartRepository).findByUserId(userId);
        verify(orderRepository, never()).persist(any(Order.class));
    }

    @Test
    void updateOrderStatus_ExistingOrder_UpdatesStatus() {
        when(orderRepository.findById(1L)).thenReturn(Uni.createFrom().item(testOrder));
        when(orderRepository.persist(any(Order.class))).thenReturn(Uni.createFrom().item(testOrder));

        Order result = orderService.updateOrderStatus(1L, OrderStatus.PAID)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(orderRepository).findById(1L);
        verify(orderRepository).persist(any(Order.class));
    }
}