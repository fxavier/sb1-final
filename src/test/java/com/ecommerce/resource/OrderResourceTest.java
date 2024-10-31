package com.ecommerce.resource;

import com.ecommerce.domain.model.Order;
import com.ecommerce.domain.model.OrderStatus;
import com.ecommerce.domain.model.ShippingAddress;
import com.ecommerce.service.OrderService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderResourceTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderResource orderResource;

    private Order testOrder;
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
        when(orderService.getUserOrders(userId)).thenReturn(Uni.createFrom().item(orders));

        Response response = orderResource.getUserOrders(userId)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(orders, response.getEntity());
        verify(orderService).getUserOrders(userId);
    }

    @Test
    void getOrder_ExistingOrder_ReturnsOrder() {
        when(orderService.getOrder(1L)).thenReturn(Uni.createFrom().item(testOrder));

        Response response = orderResource.getOrder(1L)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testOrder, response.getEntity());
        verify(orderService).getOrder(1L);
    }

    @Test
    void createOrder_ValidOrder_CreatesOrder() {
        when(orderService.createOrder(userId, shippingAddress)).thenReturn(Uni.createFrom().item(testOrder));

        Response response = orderResource.createOrder(userId, shippingAddress)
            .await().indefinitely();

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testOrder, response.getEntity());
        verify(orderService).createOrder(userId, shippingAddress);
    }

    @Test
    void updateOrderStatus_ValidStatus_UpdatesStatus() {
        when(orderService.updateOrderStatus(1L, OrderStatus.PAID)).thenReturn(Uni.createFrom().item(testOrder));

        Response response = orderResource.updateOrderStatus(1L, OrderStatus.PAID)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testOrder, response.getEntity());
        verify(orderService).updateOrderStatus(1L, OrderStatus.PAID);
    }
}