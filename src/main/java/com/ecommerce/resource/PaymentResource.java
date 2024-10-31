package com.ecommerce.resource;

import com.ecommerce.domain.dto.PaymentIntentDTO;
import com.ecommerce.domain.dto.PaymentResponseDTO;
import com.ecommerce.service.PaymentService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {
    
    @Inject
    PaymentService paymentService;
    
    @POST
    @Path("/create-intent")
    public Uni<Response> createPaymentIntent(@Valid PaymentIntentDTO paymentDTO) {
        return paymentService.createPaymentIntent(paymentDTO)
            .onItem().transform(response -> Response.ok(response).build());
    }
    
    @POST
    @Path("/confirm/{paymentIntentId}")
    public Uni<Response> confirmPayment(@PathParam("paymentIntentId") String paymentIntentId) {
        return paymentService.confirmPayment(paymentIntentId)
            .onItem().transform(response -> Response.ok(response).build());
    }
    
    @POST
    @Path("/webhook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> handleWebhook(
            String payload,
            @HeaderParam("Stripe-Signature") String sigHeader) {
        return paymentService.handleWebhook(payload, sigHeader)
            .onItem().transform(v -> Response.ok().build());
    }
}