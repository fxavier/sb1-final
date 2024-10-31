package com.ecommerce.service;

import com.ecommerce.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class EmailService {
    
    @ConfigProperty(name = "app.base-url")
    String baseUrl;
    
    @ConfigProperty(name = "mail.smtp.host")
    String smtpHost;
    
    @ConfigProperty(name = "mail.smtp.port")
    String smtpPort;
    
    @ConfigProperty(name = "mail.smtp.username")
    String smtpUsername;
    
    @ConfigProperty(name = "mail.smtp.password")
    String smtpPassword;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    public void sendPasswordResetEmail(User user) throws MessagingException {
        String resetLink = baseUrl + "/reset-password?token=" + user.getResetToken();
        String htmlContent = buildPasswordResetEmail(user.getProfile().getFirstName(), resetLink);
        
        sendEmail(user.getEmail(), "Reset Your Password", htmlContent);
    }
    
    public void sendVerificationEmailAsync(User user) {
        CompletableFuture.runAsync(() -> {
            try {
                String verificationLink = baseUrl + "/verify-email?token=" + user.getVerificationToken();
                String htmlContent = buildVerificationEmail(user.getProfile().getFirstName(), verificationLink);
                sendEmail(user.getEmail(), "Verify Your Email", htmlContent);
            } catch (MessagingException e) {
                // Log error but don't throw to keep async nature
                e.printStackTrace();
            }
        }, executorService);
    }
    
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        
        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");
        
        Transport.send(message);
    }
    
    private String buildPasswordResetEmail(String firstName, String resetLink) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #333333; margin-bottom: 10px;">Reset Your Password</h1>
                        <p style="color: #666666; font-size: 16px;">Hello %s,</p>
                        <p style="color: #666666; font-size: 16px;">We received a request to reset your password.</p>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; 
                                  border-radius: 4px; font-size: 16px; display: inline-block;">
                            Reset Password
                        </a>
                    </div>
                    
                    <div style="text-align: center; margin-top: 20px;">
                        <p style="color: #666666; font-size: 14px;">
                            If you didn't request this, you can safely ignore this email.
                        </p>
                        <p style="color: #666666; font-size: 14px;">
                            This link will expire in 1 hour.
                        </p>
                    </div>
                </div>
            </div>
            """.formatted(firstName, resetLink);
    }
    
    private String buildVerificationEmail(String firstName, String verificationLink) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #333333; margin-bottom: 10px;">Verify Your Email</h1>
                        <p style="color: #666666; font-size: 16px;">Hello %s,</p>
                        <p style="color: #666666; font-size: 16px;">Please verify your email address to complete your registration.</p>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; 
                                  border-radius: 4px; font-size: 16px; display: inline-block;">
                            Verify Email
                        </a>
                    </div>
                    
                    <div style="text-align: center; margin-top: 20px;">
                        <p style="color: #666666; font-size: 14px;">
                            If you didn't create an account, you can safely ignore this email.
                        </p>
                    </div>
                </div>
            </div>
            """.formatted(firstName, verificationLink);
    }
}