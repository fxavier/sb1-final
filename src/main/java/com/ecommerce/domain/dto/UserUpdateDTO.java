package com.ecommerce.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phoneNumber;
    
    private LocalDate birthdate;
    private AddressDTO address;
}