package com.example.chatapp.service;

import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        if (userRepository.findByUsernameAndIsActiveTrue(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmailAndIsActiveTrue(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        user.setActive(true);
        return userRepository.save(user);
    }

    public List<User> getAllUsers(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {

            return userRepository.findAll()
                    .stream()
                    .filter(User::isActive)
                    .toList();
        }


        return userRepository.findAll()
                .stream()
                .filter(User::isActive)
                .filter(user -> user.getUsername().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(searchQuery.toLowerCase()))
                .toList();
    }

    public User getUserById(Long id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + id + " not found."));
    }

    public User updateUser(Long actorId,Long id, User userDetails) {
        if (!actorId.equals(id)) {
            throw new IllegalArgumentException("You are not authorized to update this user.");
        }
        User existingUser = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + id + " not found."));

        if (userDetails.getUsername() != null &&
                !userDetails.getUsername().equals(existingUser.getUsername()) &&
                userRepository.findByUsernameAndIsActiveTrue(userDetails.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + userDetails.getUsername() + "' already exists.");
        }


        if (userDetails.getEmail() != null &&
                !userDetails.getEmail().equals(existingUser.getEmail()) &&
                userRepository.findByEmailAndIsActiveTrue(userDetails.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + userDetails.getEmail() + "' already exists.");
        }
        existingUser.setUsername(userDetails.getUsername() != null ? userDetails.getUsername() : existingUser.getUsername());
        existingUser.setEmail(userDetails.getEmail() != null ? userDetails.getEmail() : existingUser.getEmail());
        existingUser.setPassword(userDetails.getPassword() != null ? userDetails.getPassword() : existingUser.getPassword());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long actorId,Long id) {
        if (!actorId.equals(id)) {
            throw new IllegalArgumentException("You are not authorized to delete this user.");
        }
        User existingUser = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + id + " not found."));
        existingUser.setActive(false);
        userRepository.save(existingUser);
    }
}
