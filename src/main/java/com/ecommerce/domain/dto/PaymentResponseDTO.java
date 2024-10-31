package com.ecommerce.domain.dto;

import lombok.Data;

@Data
public class PaymentResponseDTO {
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    
    public PaymentResponseDTO(String clientSecret, String paymentIntentId, String status) {
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.status = status;
    }
}