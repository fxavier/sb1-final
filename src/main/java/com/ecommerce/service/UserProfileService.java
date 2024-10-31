package com.ecommerce.service;

import com.ecommerce.domain.dto.UserDTO;
import com.ecommerce.domain.dto.UserUpdateDTO;
import com.ecommerce.domain.model.User;
import com.ecommerce.domain.model.UserProfile;
import com.ecommerce.domain.model.Address;
import com.ecommerce.domain.repository.UserRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class UserProfileService {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    ImageStorageService imageStorageService;
    
    public Uni<List<User>> getAllUsers() {
        return userRepository.listAll();
    }
    
    public Uni<User> getUserById(Long id) {
        return userRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("User not found"));
    }
    
    public Uni<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("User not found"));
    }
    
    @Transactional
    public Uni<User> updateUser(Long id, UserUpdateDTO updateDTO) {
        return userRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("User not found"))
            .chain(user -> {
                updateUserFromDto(user, updateDTO);
                return userRepository.persist(user);
            });
    }
    
    @Transactional
    public Uni<Boolean> deleteUser(Long id) {
        return userRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("User not found"))
            .chain(user -> userRepository.delete(user)
                .map(deleted -> deleted > 0));
    }
    
    @Transactional
    public Uni<String> updateAvatar(Long userId, String imageUrl) {
        return userRepository.findById(userId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("User not found"))
            .chain(user -> {
                UserProfile profile = user.getProfile();
                if (profile == null) {
                    profile = new UserProfile();
                    profile.setUser(user);
                    user.setProfile(profile);
                }
                
                String oldAvatar = profile.getAvatar();
                profile.setAvatar(imageUrl);
                
                return userRepository.persist(user)
                    .chain(() -> {
                        if (oldAvatar != null) {
                            return imageStorageService.deleteImage(oldAvatar)
                                .map(v -> imageUrl);
                        }
                        return Uni.createFrom().item(imageUrl);
                    });
            });
    }
    
    private void updateUserFromDto(User user, UserUpdateDTO dto) {
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setBirthdate(dto.getBirthdate());
        
        if (dto.getPhoneNumber() != null && 
            !dto.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setPhoneVerified(false);
        }
        
        if (dto.getAddress() != null) {
            Address address = profile.getAddress();
            if (address == null) {
                address = new Address();
                profile.setAddress(address);
            }
            
            address.setStreet(dto.getAddress().getStreet());
            address.setCity(dto.getAddress().getCity());
            address.setState(dto.getAddress().getState());
            address.setCountry(dto.getAddress().getCountry());
            address.setPostalCode(dto.getAddress().getPostalCode());
        }
    }
    
    public UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setPhoneVerified(user.isPhoneVerified());
        
        UserProfile profile = user.getProfile();
        if (profile != null) {
            dto.setFirstName(profile.getFirstName());
            dto.setLastName(profile.getLastName());
            dto.setBirthdate(profile.getBirthdate());
            dto.setAvatar(profile.getAvatar());
            
            if (profile.getAddress() != null) {
                AddressDTO addressDTO = new AddressDTO();
                addressDTO.setStreet(profile.getAddress().getStreet());
                addressDTO.setCity(profile.getAddress().getCity());
                addressDTO.setState(profile.getAddress().getState());
                addressDTO.setCountry(profile.getAddress().getCountry());
                addressDTO.setPostalCode(profile.getAddress().getPostalCode());
                dto.setAddress(addressDTO);
            }
        }
        
        return dto;
    }
}