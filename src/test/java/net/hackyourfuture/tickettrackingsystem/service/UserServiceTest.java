package net.hackyourfuture.tickettrackingsystem.service;

import net.hackyourfuture.tickettrackingsystem.dto.UserDTO;
import net.hackyourfuture.tickettrackingsystem.exceptions.ResourceNotFoundException;
import net.hackyourfuture.tickettrackingsystem.models.User;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Create simple mock objects
        userRepository = Mockito.mock(UserRepository.class);

        // Pass the mock so the constructor matches the service file exactly
        userService = new UserService(userRepository);
    }

    @Test
    void testCreateUser_ShouldReturnSavedUser() {
        UserDTO inputDto = new UserDTO(null, "John Doe", "john@example.com");
        User mockSavedUser = new User(1L, "John Doe", "john@example.com");

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(mockSavedUser);

        UserDTO result = userService.createUser(inputDto);

        assertNotNull(result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetUserById_ShouldThrowException_WhenUserMissing() {
        Long missingId = 404L;
        Mockito.when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(missingId);
        });
    }

    @Test
    void testDeleteUser_ShouldThrowException_WhenUserMissing() {
        Long missingId = 404L;
        Mockito.when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(missingId);
        });
    }

    @Test
    void testUpdateUser_ShouldThrowException_WhenUserMissing() {
        Long missingId = 404L;
        UserDTO inputDto = new UserDTO(null, "New Name", "new@example.com");

        Mockito.when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(missingId, inputDto);
        });
    }
}