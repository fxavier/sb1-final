package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.StockAlert;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class StockAlertRepository implements PanacheRepository<StockAlert> {
    
    public Uni<List<StockAlert>> findActiveAlerts() {
        return list("active", true);
    }
    
    public Uni<StockAlert> findByProduct(Long productId) {
        return find("product.id", productId).firstResult();
    }
}