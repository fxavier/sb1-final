package com.ecommerce.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class StockAlertDTO {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Threshold is required")
    @Positive(message = "Threshold must be positive")
    private Integer threshold;
    
    private Boolean active = true;
}