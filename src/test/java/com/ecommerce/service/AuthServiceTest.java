package com.ecommerce.service;

import com.ecommerce.domain.dto.SignupDTO;
import com.ecommerce.domain.dto.SigninDTO;
import com.ecommerce.domain.model.User;
import com.ecommerce.domain.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private SignupDTO signupDTO;
    private SigninDTO signinDTO;
    private User testUser;

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

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        testUser.setEmailVerified(true);
    }

    @Test
    void signup_NewUser_CreatesUser() {
        when(userRepository.existsByEmail(signupDTO.getEmail())).thenReturn(Uni.createFrom().item(false));
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(testUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("test-token");
        when(emailService.sendVerificationEmail(any(User.class))).thenReturn(Uni.createFrom().nullItem());

        var result = authService.signup(signupDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        verify(userRepository).existsByEmail(signupDTO.getEmail());
        verify(userRepository).persist(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void signup_ExistingEmail_ThrowsException() {
        when(userRepository.existsByEmail(signupDTO.getEmail())).thenReturn(Uni.createFrom().item(true));

        assertThrows(IllegalStateException.class, () -> {
            authService.signup(signupDTO).await().indefinitely();
        });
        verify(userRepository).existsByEmail(signupDTO.getEmail());
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void signin_ValidCredentials_ReturnsToken() {
        when(userRepository.findByEmail(signinDTO.getEmail())).thenReturn(Uni.createFrom().item(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("test-token");

        var result = authService.signin(signinDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        verify(userRepository).findByEmail(signinDTO.getEmail());
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void signin_InvalidCredentials_ThrowsException() {
        signinDTO.setPassword("wrongpassword");
        when(userRepository.findByEmail(signinDTO.getEmail())).thenReturn(Uni.createFrom().item(testUser));

        assertThrows(SecurityException.class, () -> {
            authService.signin(signinDTO).await().indefinitely();
        });
        verify(userRepository).findByEmail(signinDTO.getEmail());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void verifyEmail_ValidToken_VerifiesEmail() {
        String token = "valid-token";
        testUser.setEmailVerified(false);
        testUser.setVerificationToken(token);
        testUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        when(userRepository.find("verificationToken", token)).thenReturn(Uni.createFrom().item(testUser));
        when(userRepository.persist(any(User.class))).thenReturn(Uni.createFrom().item(testUser));

        Boolean result = authService.verifyEmail(token)
            .await().indefinitely();

        assertTrue(result);
        assertTrue(testUser.isEmailVerified());
        assertNull(testUser.getVerificationToken());
        assertNull(testUser.getVerificationTokenExpiry());
        verify(userRepository).persist(testUser);
    }
}