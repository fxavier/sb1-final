package com.ecommerce.resource;

import com.ecommerce.domain.dto.UserDTO;
import com.ecommerce.domain.dto.UserUpdateDTO;
import com.ecommerce.service.UserProfileService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestForm;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    
    @Inject
    UserProfileService userProfileService;
    
    @Inject
    ImageStorageService imageStorageService;
    
    @GET
    public Uni<Response> getAllUsers() {
        return userProfileService.getAllUsers()
            .map(users -> users.stream()
                .map(user -> userProfileService.mapToDTO(user))
                .collect(Collectors.toList()))
            .map(userDTOs -> Response.ok(userDTOs).build());
    }
    
    @GET
    @Path("/{id}")
    public Uni<Response> getUser(@PathParam("id") Long id) {
        return userProfileService.getUserById(id)
            .map(user -> userProfileService.mapToDTO(user))
            .map(userDTO -> Response.ok(userDTO).build());
    }
    
    @PUT
    @Path("/{id}")
    public Uni<Response> updateUser(
            @PathParam("id") Long id,
            @Valid UserUpdateDTO updateDTO) {
        return userProfileService.updateUser(id, updateDTO)
            .map(user -> userProfileService.mapToDTO(user))
            .map(userDTO -> Response.ok(userDTO).build());
    }
    
    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteUser(@PathParam("id") Long id) {
        return userProfileService.deleteUser(id)
            .map(deleted -> deleted ? Response.noContent().build() 
                                  : Response.status(Response.Status.NOT_FOUND).build());
    }
    
    @POST
    @Path("/{id}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Response> updateAvatar(
            @PathParam("id") Long id,
            @MultipartForm FileUploadDTO fileUpload) {
        return imageStorageService.uploadImage(
                fileUpload.getFile().fileName(),
                fileUpload.getFile().uploadedFile().toFile(),
                fileUpload.getFile().contentType(),
                fileUpload.getFile().size())
            .chain(imageUrl -> userProfileService.updateAvatar(id, imageUrl))
            .map(imageUrl -> Response.ok(new ImageUploadResponse(imageUrl)).build());
    }
    
    public static class FileUploadDTO {
        @RestForm("file")
        public org.jboss.resteasy.reactive.multipart.FileUpload file;
        
        public org.jboss.resteasy.reactive.multipart.FileUpload getFile() {
            return file;
        }
        
        public void setFile(org.jboss.resteasy.reactive.multipart.FileUpload file) {
            this.file = file;
        }
    }
    
    public static class ImageUploadResponse {
        private final String imageUrl;
        
        public ImageUploadResponse(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
    }
}