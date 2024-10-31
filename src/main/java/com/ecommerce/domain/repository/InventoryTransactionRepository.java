package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.InventoryTransaction;
import com.ecommerce.domain.model.TransactionType;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class InventoryTransactionRepository implements PanacheRepository<InventoryTransaction> {
    
    public Uni<List<InventoryTransaction>> findByProduct(Long productId) {
        return list("product.id", productId);
    }
    
    public Uni<List<InventoryTransaction>> findByType(TransactionType type) {
        return list("type", type);
    }
    
    public Uni<List<InventoryTransaction>> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return list("timestamp between ?1 and ?2", start, end);
    }
}