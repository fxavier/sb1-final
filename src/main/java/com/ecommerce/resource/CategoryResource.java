package com.ecommerce.resource;

import com.ecommerce.domain.dto.CategoryDTO;
import com.ecommerce.service.CategoryService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {
    
    @Inject
    CategoryService categoryService;
    
    @GET
    public Uni<Response> getAllCategories() {
        return categoryService.getAllCategories()
            .onItem().transform(categories -> Response.ok(categories).build());
    }
    
    @GET
    @Path("/root")
    public Uni<Response> getRootCategories() {
        return categoryService.getRootCategories()
            .onItem().transform(categories -> Response.ok(categories).build());
    }
    
    @GET
    @Path("/{id}/subcategories")
    public Uni<Response> getSubcategories(@PathParam("id") Long id) {
        return categoryService.getSubcategories(id)
            .onItem().transform(categories -> Response.ok(categories).build());
    }
    
    @GET
    @Path("/{id}")
    public Uni<Response> getCategory(@PathParam("id") Long id) {
        return categoryService.getCategory(id)
            .onItem().transform(category -> Response.ok(category).build());
    }
    
    @POST
    public Uni<Response> createCategory(@Valid CategoryDTO categoryDTO) {
        return categoryService.createCategory(categoryDTO)
            .onItem().transform(category -> 
                Response.status(Response.Status.CREATED).entity(category).build());
    }
    
    @PUT
    @Path("/{id}")
    public Uni<Response> updateCategory(
            @PathParam("id") Long id,
            @Valid CategoryDTO categoryDTO) {
        return categoryService.updateCategory(id, categoryDTO)
            .onItem().transform(category -> Response.ok(category).build());
    }
    
    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteCategory(@PathParam("id") Long id) {
        return categoryService.deleteCategory(id)
            .onItem().transform(deleted -> 
                deleted ? Response.noContent().build() 
                       : Response.status(Response.Status.NOT_FOUND).build());
    }
}