package com.ecommerce.service;

import com.ecommerce.domain.model.User;
import com.ecommerce.domain.repository.UserRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class PasswordResetService {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    EmailService emailService;
    
    public Uni<Void> initiatePasswordReset(String email) {
        return userRepository.findByEmail(email)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("User not found"))
            .chain(user -> {
                user.setResetToken(UUID.randomUUID().toString());
                user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
                
                return userRepository.persist(user)
                    .chain(savedUser -> emailService.sendPasswordResetEmail(savedUser))
                    .replaceWith(null);
            });
    }
    
    public Uni<Boolean> resetPassword(String token, String newPassword) {
        return userRepository.find("resetToken", token)
            .firstResult()
            .chain(user -> {
                if (user == null) {
                    return Uni.createFrom().item(false);
                }
                
                if (LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
                    return Uni.createFrom().item(false);
                }
                
                user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                
                return userRepository.persist(user)
                    .map(savedUser -> true);
            });
    }
}