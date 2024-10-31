package com.ecommerce.service;

import com.ecommerce.domain.dto.InventoryTransactionDTO;
import com.ecommerce.domain.dto.StockAlertDTO;
import com.ecommerce.domain.model.*;
import com.ecommerce.domain.repository.*;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class InventoryService {
    
    @Inject
    ProductRepository productRepository;
    
    @Inject
    InventoryTransactionRepository transactionRepository;
    
    @Inject
    StockAlertRepository alertRepository;
    
    @Inject
    EmailService emailService;
    
    @Transactional
    public Uni<InventoryTransaction> recordTransaction(InventoryTransactionDTO transactionDTO) {
        return productRepository.findById(transactionDTO.getProductId())
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                // Calculate new stock level
                int newStock = calculateNewStock(
                    product.getStockQuantity(),
                    transactionDTO.getQuantity(),
                    transactionDTO.getType()
                );
                
                // Validate stock level
                if (newStock < 0) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Insufficient stock"));
                }
                
                // Update product stock
                product.setStockQuantity(newStock);
                
                // Create transaction record
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProduct(product);
                transaction.setQuantity(transactionDTO.getQuantity());
                transaction.setType(transactionDTO.getType());
                transaction.setReference(transactionDTO.getReference());
                
                // Check for low stock
                checkLowStock(product, newStock);
                
                return productRepository.persist(product)
                    .chain(() -> transactionRepository.persist(transaction));
            });
    }
    
    private int calculateNewStock(int currentStock, int quantity, TransactionType type) {
        return switch (type) {
            case PURCHASE, RESTOCK, RETURN -> currentStock + quantity;
            case SALE, DAMAGED -> currentStock - quantity;
            case ADJUSTMENT -> quantity;
        };
    }
    
    private void checkLowStock(Product product, int newStock) {
        alertRepository.findByProduct(product.getId())
            .subscribe().with(alert -> {
                if (alert != null && alert.getActive() && 
                    newStock <= alert.getThreshold()) {
                    emailService.sendLowStockAlert(product, newStock, alert.getThreshold())
                        .subscribe().with(
                            success -> {},
                            error -> System.err.println("Failed to send low stock alert: " + error)
                        );
                }
            });
    }
    
    public Uni<List<InventoryTransaction>> getProductTransactions(Long productId) {
        return transactionRepository.findByProduct(productId);
    }
    
    public Uni<List<Product>> getLowStockProducts() {
        return productRepository.find("stockQuantity <= lowStockThreshold")
            .list();
    }
    
    @Transactional
    public Uni<StockAlert> createStockAlert(StockAlertDTO alertDTO) {
        return productRepository.findById(alertDTO.getProductId())
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                StockAlert alert = new StockAlert();
                alert.setProduct(product);
                alert.setThreshold(alertDTO.getThreshold());
                alert.setActive(alertDTO.getActive());
                
                return alertRepository.persist(alert);
            });
    }
    
    @Transactional
    public Uni<StockAlert> updateStockAlert(Long alertId, StockAlertDTO alertDTO) {
        return alertRepository.findById(alertId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Stock alert not found"))
            .chain(alert -> {
                alert.setThreshold(alertDTO.getThreshold());
                alert.setActive(alertDTO.getActive());
                
                return alertRepository.persist(alert);
            });
    }
    
    public Uni<List<StockAlert>> getActiveAlerts() {
        return alertRepository.findActiveAlerts();
    }
}