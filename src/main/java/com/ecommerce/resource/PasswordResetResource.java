package com.ecommerce.resource;

import com.ecommerce.domain.dto.PasswordResetRequestDTO;
import com.ecommerce.domain.dto.PasswordResetDTO;
import com.ecommerce.service.PasswordResetService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/password-reset")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PasswordResetResource {
    
    @Inject
    PasswordResetService passwordResetService;
    
    @POST
    @Path("/request")
    public Uni<Response> requestPasswordReset(@Valid PasswordResetRequestDTO requestDTO) {
        return passwordResetService.initiatePasswordReset(requestDTO.getEmail())
            .onItem().transform(v -> Response.ok()
                .entity(new MessageResponse("Password reset email sent"))
                .build());
    }
    
    @POST
    @Path("/reset")
    public Uni<Response> resetPassword(@Valid PasswordResetDTO resetDTO) {
        return passwordResetService.resetPassword(resetDTO.getToken(), resetDTO.getNewPassword())
            .onItem().transform(success -> 
                success ? Response.ok()
                    .entity(new MessageResponse("Password successfully reset"))
                    .build()
                : Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Invalid or expired reset token"))
                    .build());
    }
    
    private static class MessageResponse {
        private final String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}