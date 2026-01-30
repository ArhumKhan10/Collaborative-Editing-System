package com.collab.userservice.service;

import com.collab.common.exception.DuplicateResourceException;
import com.collab.common.exception.ResourceNotFoundException;
import com.collab.common.exception.UnauthorizedException;
import com.collab.common.util.JwtUtil;
import com.collab.userservice.dto.*;
import com.collab.userservice.model.User;
import com.collab.userservice.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
    }

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        User savedUser = new User(request.getUsername(), request.getEmail(), "hashedPassword");
        savedUser.setId(new ObjectId());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDTO result = userService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        String hashedPassword = passwordEncoder.encode("password123");
        User user = new User("testuser", "test@example.com", hashedPassword);
        user.setId(new ObjectId());

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyString(), anyString())).thenReturn("mock-jwt-token");

        // Act
        LoginResponse result = userService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals("mock-jwt-token", result.getToken());
        assertEquals(user.getUsername(), result.getUsername());
        verify(jwtUtil, times(1)).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void login_InvalidEmail_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("invalid@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void getUserProfile_Success() {
        // Arrange
        ObjectId userId = new ObjectId();
        User user = new User("testuser", "test@example.com", "hashedPassword");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.getUserProfile(userId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void getUserProfile_NotFound_ThrowsException() {
        // Arrange
        ObjectId userId = new ObjectId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> userService.getUserProfile(userId.toString()));
    }

    @Test
    void updateUserProfile_Success() {
        // Arrange
        ObjectId userId = new ObjectId();
        User user = new User("oldusername", "test@example.com", "hashedPassword");
        user.setId(userId);

        UpdateProfileRequest request = new UpdateProfileRequest("newusername", "https://avatar.com/pic.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDTO result = userService.updateUserProfile(userId.toString(), request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail(email);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByUsername_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        // Act
        boolean result = userService.existsByUsername(username);

        // Assert
        assertFalse(result);
    }
}
