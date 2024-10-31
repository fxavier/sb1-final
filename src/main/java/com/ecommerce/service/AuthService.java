package com.ecommerce.service;

import com.ecommerce.domain.dto.SignupDTO;
import com.ecommerce.domain.dto.SigninDTO;
import com.ecommerce.domain.dto.AuthResponseDTO;
import com.ecommerce.domain.model.User;
import com.ecommerce.domain.model.UserProfile;
import com.ecommerce.domain.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class AuthService {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    JwtService jwtService;
    
    @Inject
    UserProfileService userProfileService;
    
    @Inject
    EmailService emailService;
    
    @Transactional
    public AuthResponseDTO signup(SignupDTO signupDTO) {
        if (userRepository.existsByEmail(signupDTO.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }
        
        User user = new User();
        user.setEmail(signupDTO.getEmail());
        user.setPassword(BCrypt.hashpw(signupDTO.getPassword(), BCrypt.gensalt()));
        user.setPhoneNumber(signupDTO.getPhoneNumber());
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(signupDTO.getFirstName());
        profile.setLastName(signupDTO.getLastName());
        user.setProfile(profile);
        
        user = userRepository.save(user);
        String token = jwtService.generateToken(user);
        UserDTO userDTO = userProfileService.mapToDTO(user);
        
        // Send verification email asynchronously
        emailService.sendVerificationEmailAsync(user);
        
        return new AuthResponseDTO(token, userDTO);
    }
    
    @Transactional
    public AuthResponseDTO signin(SigninDTO signinDTO) {
        User user = userRepository.findByEmail(signinDTO.getEmail())
            .orElseThrow(() -> new SecurityException("Invalid credentials"));
            
        if (!BCrypt.checkpw(signinDTO.getPassword(), user.getPassword())) {
            throw new SecurityException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(user);
        UserDTO userDTO = userProfileService.mapToDTO(user);
        return new AuthResponseDTO(token, userDTO);
    }
    
    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new IllegalStateException("Invalid verification token"));
            
        if (LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
            throw new IllegalStateException("Verification token expired");
        }
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        
        userRepository.save(user);
        return true;
    }
}