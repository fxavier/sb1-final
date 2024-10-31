package com.ecommerce.resource;

import com.ecommerce.domain.dto.ProductWithImagesDTO;
import com.ecommerce.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    private ProductService productService;

    @GET
    public Response getAllProducts() {
        List<ProductWithImagesDTO> products = productService.getAllActiveProducts();
        return Response.ok(products).build();
    }

    @GET
    @Path("/category/{categoryId}")
    public Response getProductsByCategory(@PathParam("categoryId") Long categoryId) {
        List<ProductWithImagesDTO> products = productService.getProductsByCategory(categoryId);
        return Response.ok(products).build();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") Long id) {
        ProductWithImagesDTO product = productService.getProductById(id);
        return Response.ok(product).build();
    }

    @GET
    @Path("/search")
    public Response searchProducts(@QueryParam("name") String name) {
        List<ProductWithImagesDTO> products = productService.searchProductsByName(name);
        return Response.ok(products).build();
    }

    @GET
    @Path("/with-cover")
    public Response getProductsWithCover(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        Page<ProductWithImagesDTO> products = productService.getProductsWithCoverImage(
            PageRequest.of(page, size));
        return Response.ok(products).build();
    }

    @GET
    @Path("/{id}/details")
    public Response getProductDetails(@PathParam("id") Long id) {
        ProductWithImagesDTO product = productService.getProductDetails(id);
        return Response.ok(product).build();
    }
}