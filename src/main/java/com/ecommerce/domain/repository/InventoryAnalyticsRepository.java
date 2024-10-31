package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.InventoryAnalytics;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class InventoryAnalyticsRepository implements PanacheRepository<InventoryAnalytics> {
    
    public Uni<List<InventoryAnalytics>> findByProductAndDateRange(
            Long productId, LocalDate startDate, LocalDate endDate) {
        return list(
            "product.id = ?1 and date between ?2 and ?3 order by date",
            productId, startDate, endDate
        );
    }
    
    public Uni<List<InventoryAnalytics>> findTopSellingProducts(
            LocalDate startDate, LocalDate endDate, int limit) {
        return find(
            "select a from InventoryAnalytics a " +
            "where a.date between ?1 and ?2 " +
            "group by a.product.id " +
            "order by sum(a.salesCount) desc",
            startDate, endDate
        ).page(0, limit).list();
    }
    
    public Uni<Double> calculateAverageTurnoverRate(
            Long productId, LocalDate startDate, LocalDate endDate) {
        return find(
            "select avg(turnoverRate) from InventoryAnalytics " +
            "where product.id = ?1 and date between ?2 and ?3",
            productId, startDate, endDate
        ).firstResult();
    }
}