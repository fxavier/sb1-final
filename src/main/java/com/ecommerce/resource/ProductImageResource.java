package com.ecommerce.resource;

import com.ecommerce.domain.model.ProductImage;
import com.ecommerce.service.ProductImageService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Path("/api/products/{productId}/images")
@Produces(MediaType.APPLICATION_JSON)
public class ProductImageResource {

    @Inject
    private ProductImageService productImageService;

    @GET
    public Response getProductImages(@PathParam("productId") Long productId) {
        List<ProductImage> images = productImageService.getProductImages(productId);
        return Response.ok(images).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImage(
            @PathParam("productId") Long productId,
            @NotNull @FormParam("file") MultipartFile file,
            @FormParam("isCover") Boolean isCover) {
        try {
            ProductImage image = productImageService.uploadImage(productId, file, isCover);
            return Response.status(Response.Status.CREATED).entity(image).build();
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Failed to process image upload: " + e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{imageId}")
    public Response deleteImage(
            @PathParam("productId") Long productId,
            @PathParam("imageId") Long imageId) {
        productImageService.deleteImage(productId, imageId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{imageId}/cover")
    public Response setCoverImage(
            @PathParam("productId") Long productId,
            @PathParam("imageId") Long imageId) {
        ProductImage image = productImageService.setCoverImage(productId, imageId);
        return Response.ok(image).build();
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}