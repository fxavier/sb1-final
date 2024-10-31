package com.ecommerce.domain.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InventoryAnalyticsDTO {
    private Long productId;
    private LocalDate date;
    private Integer startingStock;
    private Integer endingStock;
    private Integer salesCount;
    private Integer restockCount;
    private Integer returnsCount;
    private Double turnoverRate;
    private Integer daysOutOfStock;
    private Integer lowStockIncidents;
}