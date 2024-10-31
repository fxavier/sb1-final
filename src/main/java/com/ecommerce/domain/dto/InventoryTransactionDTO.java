package com.ecommerce.domain.dto;

import com.ecommerce.domain.model.TransactionType;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class InventoryTransactionDTO {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    private String reference;
}