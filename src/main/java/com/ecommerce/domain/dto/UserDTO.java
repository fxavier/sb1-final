package com.ecommerce.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthdate;
    private String avatar;
    private AddressDTO address;
    private Boolean emailVerified;
    private Boolean phoneVerified;
}