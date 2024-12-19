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

    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(User::isActive)
                .toList();
    }

    public User getUserById(Long id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + id + " not found."));
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + id + " not found."));

        existingUser.setUsername(userDetails.getUsername() != null ? userDetails.getUsername() : existingUser.getUsername());
        existingUser.setEmail(userDetails.getEmail() != null ? userDetails.getEmail() : existingUser.getEmail());
        existingUser.setPassword(userDetails.getPassword() != null ? userDetails.getPassword() : existingUser.getPassword());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User existingUser = userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + id + " not found."));
        existingUser.setActive(false);
        userRepository.save(existingUser);
    }
}
