package net.hackyourfuture.tickettrackingsystem.service;

import net.hackyourfuture.tickettrackingsystem.dto.UserDTO;
import net.hackyourfuture.tickettrackingsystem.exceptions.ResourceNotFoundException;
import net.hackyourfuture.tickettrackingsystem.models.User;
import net.hackyourfuture.tickettrackingsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private static final String USER_NOT_FOUND_MSG = "User not found with id: ";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // create User
    public UserDTO createUser(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    // delete User
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + id));

        userRepository.deleteById(id);
    }
    // get all users
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }
    // get user by id
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + id));
        return mapToDTO(user);
    }
    // update user
    public UserDTO updateUser(Long id, UserDTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MSG + id));

        existingUser.setName(dto.getName());
        existingUser.setEmail(dto.getEmail());

        // Use the dedicated update method, then return the updated user object
        userRepository.update(existingUser);
        return mapToDTO(existingUser);
    }

    // Helper mapping method
    private UserDTO mapToDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}