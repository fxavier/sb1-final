package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Category;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CategoryRepository implements PanacheRepository<Category> {
    
    public Uni<List<Category>> findRootCategories() {
        return list("parent is null");
    }
    
    public Uni<List<Category>> findByParent(Long parentId) {
        return list("parent.id", parentId);
    }
    
    public Uni<List<Category>> findActive() {
        return list("active", true);
    }
    
    public Uni<Boolean> hasProducts(Long categoryId) {
        return find("select count(p) > 0 from Product p where p.category.id = ?1", categoryId)
            .firstResult();
    }
}