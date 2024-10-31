package com.ecommerce.resource;

import com.ecommerce.service.InventoryAnalyticsService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;

@Path("/api/inventory/analytics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryAnalyticsResource {
    
    @Inject
    InventoryAnalyticsService analyticsService;
    
    @GET
    @Path("/product/{productId}")
    public Uni<Response> getProductAnalytics(
            @PathParam("productId") Long productId,
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate) {
        return analyticsService.getProductAnalytics(productId, startDate, endDate)
            .onItem().transform(analytics -> Response.ok(analytics).build());
    }
    
    @GET
    @Path("/top-selling")
    public Uni<Response> getTopSellingProducts(
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        return analyticsService.getTopSellingProducts(startDate, endDate, limit)
            .onItem().transform(products -> Response.ok(products).build());
    }
    
    @GET
    @Path("/stockout-risk")
    public Uni<Response> getStockoutRisk() {
        return analyticsService.getStockoutRisk()
            .onItem().transform(risks -> Response.ok(risks).build());
    }
    
    @GET
    @Path("/health")
    public Uni<Response> getInventoryHealth() {
        return analyticsService.getInventoryHealth()
            .onItem().transform(health -> Response.ok(health).build());
    }
}