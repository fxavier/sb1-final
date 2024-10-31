package com.ecommerce.service;

import com.ecommerce.domain.model.*;
import com.ecommerce.domain.repository.*;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventoryAnalyticsService {
    
    @Inject
    InventoryAnalyticsRepository analyticsRepository;
    
    @Inject
    ProductRepository productRepository;
    
    public Uni<Map<String, Object>> getProductAnalytics(
            Long productId, LocalDate startDate, LocalDate endDate) {
        return analyticsRepository.findByProductAndDateRange(productId, startDate, endDate)
            .map(analytics -> {
                Map<String, Object> result = new HashMap<>();
                
                // Calculate metrics
                DoubleSummaryStatistics turnoverStats = analytics.stream()
                    .mapToDouble(InventoryAnalytics::getTurnoverRate)
                    .summaryStatistics();
                
                int totalSales = analytics.stream()
                    .mapToInt(InventoryAnalytics::getSalesCount)
                    .sum();
                
                int totalRestocks = analytics.stream()
                    .mapToInt(InventoryAnalytics::getRestockCount)
                    .sum();
                
                int totalReturns = analytics.stream()
                    .mapToInt(InventoryAnalytics::getReturnsCount)
                    .sum();
                
                int totalOutOfStock = analytics.stream()
                    .mapToInt(InventoryAnalytics::getDaysOutOfStock)
                    .sum();
                
                int totalLowStock = analytics.stream()
                    .mapToInt(InventoryAnalytics::getLowStockIncidents)
                    .sum();
                
                // Daily trends
                Map<LocalDate, Integer> salesTrend = analytics.stream()
                    .collect(Collectors.toMap(
                        InventoryAnalytics::getDate,
                        InventoryAnalytics::getSalesCount
                    ));
                
                Map<LocalDate, Integer> stockLevels = analytics.stream()
                    .collect(Collectors.toMap(
                        InventoryAnalytics::getDate,
                        InventoryAnalytics::getEndingStock
                    ));
                
                // Compile results
                result.put("averageTurnover", turnoverStats.getAverage());
                result.put("maxTurnover", turnoverStats.getMax());
                result.put("minTurnover", turnoverStats.getMin());
                result.put("totalSales", totalSales);
                result.put("totalRestocks", totalRestocks);
                result.put("totalReturns", totalReturns);
                result.put("daysOutOfStock", totalOutOfStock);
                result.put("lowStockIncidents", totalLowStock);
                result.put("salesTrend", salesTrend);
                result.put("stockLevels", stockLevels);
                
                return result;
            });
    }
    
    public Uni<List<Map<String, Object>>> getTopSellingProducts(
            LocalDate startDate, LocalDate endDate, int limit) {
        return analyticsRepository.findTopSellingProducts(startDate, endDate, limit)
            .map(analytics -> analytics.stream()
                .map(a -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", a.getProduct().getId());
                    product.put("productName", a.getProduct().getName());
                    product.put("totalSales", a.getSalesCount());
                    product.put("turnoverRate", a.getTurnoverRate());
                    product.put("averageStock", 
                        (a.getStartingStock() + a.getEndingStock()) / 2.0);
                    return product;
                })
                .collect(Collectors.toList()));
    }
    
    public Uni<List<Map<String, Object>>> getStockoutRisk() {
        return productRepository.find("stockQuantity <= lowStockThreshold and active = true")
            .list()
            .map(products -> products.stream()
                .map(p -> {
                    Map<String, Object> risk = new HashMap<>();
                    risk.put("productId", p.getId());
                    risk.put("productName", p.getName());
                    risk.put("currentStock", p.getStockQuantity());
                    risk.put("threshold", p.getLowStockThreshold());
                    risk.put("riskLevel", calculateRiskLevel(p));
                    return risk;
                })
                .collect(Collectors.toList()));
    }
    
    private String calculateRiskLevel(Product product) {
        double stockRatio = (double) product.getStockQuantity() / product.getLowStockThreshold();
        if (stockRatio <= 0.25) return "CRITICAL";
        if (stockRatio <= 0.5) return "HIGH";
        if (stockRatio <= 0.75) return "MEDIUM";
        return "LOW";
    }
    
    public Uni<Map<String, Object>> getInventoryHealth() {
        return productRepository.findAll().list()
            .map(products -> {
                Map<String, Object> health = new HashMap<>();
                
                long totalProducts = products.size();
                long lowStockProducts = products.stream()
                    .filter(p -> p.getStockQuantity() <= p.getLowStockThreshold())
                    .count();
                long outOfStockProducts = products.stream()
                    .filter(p -> p.getStockQuantity() == 0)
                    .count();
                
                double totalValue = products.stream()
                    .mapToDouble(p -> p.getPrice().doubleValue() * p.getStockQuantity())
                    .sum();
                
                health.put("totalProducts", totalProducts);
                health.put("lowStockProducts", lowStockProducts);
                health.put("outOfStockProducts", outOfStockProducts);
                health.put("totalInventoryValue", totalValue);
                health.put("healthScore", calculateHealthScore(
                    totalProducts, lowStockProducts, outOfStockProducts));
                
                return health;
            });
    }
    
    private double calculateHealthScore(
            long totalProducts, long lowStockProducts, long outOfStockProducts) {
        if (totalProducts == 0) return 0.0;
        
        double lowStockImpact = (lowStockProducts / (double) totalProducts) * 30;
        double outOfStockImpact = (outOfStockProducts / (double) totalProducts) * 70;
        
        return 100 - (lowStockImpact + outOfStockImpact);
    }
}