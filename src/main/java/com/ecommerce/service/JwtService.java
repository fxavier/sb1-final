package com.ecommerce.service;

import com.ecommerce.domain.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Duration;
import java.util.HashSet;

@ApplicationScoped
public class JwtService {
    
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;
    
    @ConfigProperty(name = "jwt.duration.hours", defaultValue = "24")
    long tokenDurationHours;
    
    public String generateToken(User user) {
        return Jwt.issuer(issuer)
            .subject(user.getId().toString())
            .groups(new HashSet<>())
            .claim("email", user.getEmail())
            .claim("emailVerified", user.isEmailVerified())
            .expiresIn(Duration.ofHours(tokenDurationHours))
            .sign();
    }
}