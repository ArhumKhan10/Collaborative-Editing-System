package com.collab.userservice.service;

import com.collab.common.exception.BadRequestException;
import com.collab.common.exception.DuplicateResourceException;
import com.collab.common.exception.ResourceNotFoundException;
import com.collab.common.exception.UnauthorizedException;
import com.collab.common.util.JwtUtil;
import com.collab.userservice.dto.*;
import com.collab.userservice.model.User;
import com.collab.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Register a new user
     */
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Create new user
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        return UserDTO.fromUser(savedUser);
    }

    /**
     * Authenticate user and generate JWT token
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail()
        );

        log.info("User logged in successfully: {}", user.getId());

        return new LoginResponse(
            token,
            user.getId().toString(),
            user.getUsername(),
            user.getEmail()
        );
    }

    /**
     * Get user profile by ID
     */
    public UserDTO getUserProfile(String userId) {
        log.info("Fetching user profile: {}", userId);

        User user = userRepository.findById(new ObjectId(userId))
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return UserDTO.fromUser(user);
    }

    /**
     * Update user profile
     */
    public UserDTO updateUserProfile(String userId, UpdateProfileRequest request) {
        log.info("Updating user profile: {}", userId);

        User user = userRepository.findById(new ObjectId(userId))
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update fields if provided
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            // Check if new username is already taken by another user
            userRepository.findByUsername(request.getUsername())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(user.getId())) {
                        throw new DuplicateResourceException("User", "username", request.getUsername());
                    }
                });
            user.setUsername(request.getUsername());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully: {}", userId);

        return UserDTO.fromUser(updatedUser);
    }

    /**
     * Change user password
     */
    public void changePassword(String userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(new ObjectId(userId))
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if user exists by username
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
