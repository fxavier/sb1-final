package com.ecommerce.resource;

import com.ecommerce.domain.dto.SignupDTO;
import com.ecommerce.domain.dto.SigninDTO;
import com.ecommerce.service.AuthService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    @Inject
    AuthService authService;
    
    @POST
    @Path("/signup")
    public Uni<Response> signup(@Valid SignupDTO signupDTO) {
        return authService.signup(signupDTO)
            .onItem().transform(authResponse -> 
                Response.status(Response.Status.CREATED)
                    .entity(authResponse)
                    .build());
    }
    
    @POST
    @Path("/signin")
    public Uni<Response> signin(@Valid SigninDTO signinDTO) {
        return authService.signin(signinDTO)
            .onItem().transform(authResponse -> 
                Response.ok(authResponse).build());
    }
    
    @GET
    @Path("/verify-email")
    public Uni<Response> verifyEmail(@QueryParam("token") String token) {
        return authService.verifyEmail(token)
            .onItem().transform(verified -> 
                verified ? Response.ok().build() 
                        : Response.status(Response.Status.BAD_REQUEST).build());
    }
}