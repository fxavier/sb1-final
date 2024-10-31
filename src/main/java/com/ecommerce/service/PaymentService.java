package com.ecommerce.service;

import com.ecommerce.domain.dto.PaymentIntentDTO;
import com.ecommerce.domain.dto.PaymentResponseDTO;
import com.ecommerce.domain.model.Order;
import com.ecommerce.domain.model.OrderStatus;
import com.ecommerce.exception.PaymentException;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PaymentService {
    
    @Inject
    OrderService orderService;
    
    @ConfigProperty(name = "stripe.api.key")
    String stripeApiKey;
    
    @ConfigProperty(name = "stripe.webhook.secret")
    String webhookSecret;
    
    public Uni<PaymentResponseDTO> createPaymentIntent(PaymentIntentDTO paymentDTO) {
        return orderService.getOrder(paymentDTO.getOrderId())
            .chain(order -> {
                if (!order.getStatus().equals(OrderStatus.PENDING)) {
                    return Uni.createFrom().failure(
                        new PaymentException("Invalid order status for payment"));
                }
                
                return Uni.createFrom().item(() -> {
                    try {
                        Stripe.apiKey = stripeApiKey;
                        
                        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                            .setAmount(paymentDTO.getAmount().multiply(new java.math.BigDecimal(100)).longValue())
                            .setCurrency(paymentDTO.getCurrency().toLowerCase())
                            .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                            )
                            .putMetadata("orderId", order.getId().toString())
                            .build();
                        
                        PaymentIntent paymentIntent = PaymentIntent.create(params);
                        
                        order.setPaymentIntentId(paymentIntent.getId());
                        return orderService.updateOrder(order)
                            .map(updatedOrder -> new PaymentResponseDTO(
                                paymentIntent.getClientSecret(),
                                paymentIntent.getId(),
                                paymentIntent.getStatus()
                            ));
                    } catch (Exception e) {
                        throw new PaymentException("Failed to create payment intent", e);
                    }
                }).flatMap(uni -> uni);
            });
    }
    
    public Uni<PaymentResponseDTO> confirmPayment(String paymentIntentId) {
        return Uni.createFrom().item(() -> {
            try {
                Stripe.apiKey = stripeApiKey;
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                
                Map<String, Object> params = new HashMap<>();
                params.put("payment_method", "pm_card_visa"); // For testing only
                
                PaymentIntent confirmedIntent = paymentIntent.confirm(params);
                
                return new PaymentResponseDTO(
                    confirmedIntent.getClientSecret(),
                    confirmedIntent.getId(),
                    confirmedIntent.getStatus()
                );
            } catch (Exception e) {
                throw new PaymentException("Failed to confirm payment", e);
            }
        });
    }
    
    public Uni<Void> handleWebhook(String payload, String sigHeader) {
        return Uni.createFrom().item(() -> {
            try {
                com.stripe.model.Event event = com.stripe.webhook.Webhook.constructEvent(
                    payload, sigHeader, webhookSecret);
                
                switch (event.getType()) {
                    case "payment_intent.succeeded":
                        handlePaymentSuccess(event);
                        break;
                    case "payment_intent.payment_failed":
                        handlePaymentFailure(event);
                        break;
                }
                
                return null;
            } catch (Exception e) {
                throw new PaymentException("Failed to handle webhook", e);
            }
        });
    }
    
    private void handlePaymentSuccess(com.stripe.model.Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject().get();
        
        String orderId = paymentIntent.getMetadata().get("orderId");
        
        orderService.updateOrderStatus(Long.parseLong(orderId), OrderStatus.PAID)
            .subscribe().with(
                success -> {},
                error -> System.err.println("Failed to update order status: " + error)
            );
    }
    
    private void handlePaymentFailure(com.stripe.model.Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject().get();
        
        String orderId = paymentIntent.getMetadata().get("orderId");
        
        orderService.updateOrderStatus(Long.parseLong(orderId), OrderStatus.PAYMENT_FAILED)
            .subscribe().with(
                success -> {},
                error -> System.err.println("Failed to update order status: " + error)
            );
    }
}