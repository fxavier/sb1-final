package com.ecommerce.resource;

import com.ecommerce.domain.dto.AuthResponseDTO;
import com.ecommerce.domain.dto.SignupDTO;
import com.ecommerce.domain.dto.SigninDTO;
import com.ecommerce.domain.dto.UserDTO;
import com.ecommerce.service.AuthService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthResource authResource;

    private SignupDTO signupDTO;
    private SigninDTO signinDTO;
    private AuthResponseDTO authResponse;

    @BeforeEach
    void setUp() {
        signupDTO = new SignupDTO();
        signupDTO.setEmail("test@example.com");
        signupDTO.setPassword("password123");
        signupDTO.setFirstName("John");
        signupDTO.setLastName("Doe");

        signinDTO = new SigninDTO();
        signinDTO.setEmail("test@example.com");
        signinDTO.setPassword("password123");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");

        authResponse = new AuthResponseDTO("test-token", userDTO);
    }

    @Test
    void signup_ValidData_CreatesUser() {
        when(authService.signup(any(SignupDTO.class))).thenReturn(Uni.createFrom().item(authResponse));

        Response response = authResource.signup(signupDTO)
            .await().indefinitely();

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(authResponse, response.getEntity());
        verify(authService).signup(signupDTO);
    }

    @Test
    void signin_ValidCredentials_ReturnsToken() {
        when(authService.signin(any(SigninDTO.class))).thenReturn(Uni.createFrom().item(authResponse));

        Response response = authResource.signin(signinDTO)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(authResponse, response.getEntity());
        verify(authService).signin(signinDTO);
    }

    @Test
    void verifyEmail_ValidToken_VerifiesEmail() {
        String token = "valid-token";
        when(authService.verifyEmail(token)).thenReturn(Uni.createFrom().item(true));

        Response response = authResource.verifyEmail(token)
            .await().indefinitely();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(authService).verifyEmail(token);
    }

    @Test
    void verifyEmail_InvalidToken_ReturnsBadRequest() {
        String token = "invalid-token";
        when(authService.verifyEmail(token)).thenReturn(Uni.createFrom().item(false));

        Response response = authResource.verifyEmail(token)
            .await().indefinitely();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(authService).verifyEmail(token);
    }
}