package com.ecommerce.resource;

import com.ecommerce.domain.dto.InventoryTransactionDTO;
import com.ecommerce.domain.dto.StockAlertDTO;
import com.ecommerce.service.InventoryService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {
    
    @Inject
    InventoryService inventoryService;
    
    @POST
    @Path("/transactions")
    public Uni<Response> recordTransaction(@Valid InventoryTransactionDTO transactionDTO) {
        return inventoryService.recordTransaction(transactionDTO)
            .onItem().transform(transaction -> 
                Response.status(Response.Status.CREATED)
                    .entity(transaction)
                    .build());
    }
    
    @GET
    @Path("/transactions/product/{productId}")
    public Uni<Response> getProductTransactions(@PathParam("productId") Long productId) {
        return inventoryService.getProductTransactions(productId)
            .onItem().transform(transactions -> 
                Response.ok(transactions).build());
    }
    
    @GET
    @Path("/low-stock")
    public Uni<Response> getLowStockProducts() {
        return inventoryService.getLowStockProducts()
            .onItem().transform(products -> 
                Response.ok(products).build());
    }
    
    @POST
    @Path("/alerts")
    public Uni<Response> createStockAlert(@Valid StockAlertDTO alertDTO) {
        return inventoryService.createStockAlert(alertDTO)
            .onItem().transform(alert -> 
                Response.status(Response.Status.CREATED)
                    .entity(alert)
                    .build());
    }
    
    @PUT
    @Path("/alerts/{alertId}")
    public Uni<Response> updateStockAlert(
            @PathParam("alertId") Long alertId,
            @Valid StockAlertDTO alertDTO) {
        return inventoryService.updateStockAlert(alertId, alertDTO)
            .onItem().transform(alert -> 
                Response.ok(alert).build());
    }
    
    @GET
    @Path("/alerts")
    public Uni<Response> getActiveAlerts() {
        return inventoryService.getActiveAlerts()
            .onItem().transform(alerts -> 
                Response.ok(alerts).build());
    }
}