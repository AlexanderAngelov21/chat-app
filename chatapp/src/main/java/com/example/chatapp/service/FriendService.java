package com.example.chatapp.service;

import com.example.chatapp.model.Friend;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.FriendRepository;
import com.example.chatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    // 1. Add a friend
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("A user cannot add themselves as a friend.");
        }

        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        User friend = userRepository.findByIdAndIsActiveTrue(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Friend not found."));

        // Check if the friendship already exists (active or inactive)
        Friend existingFriendship = friendRepository.findByUserAndFriend(user, friend)
                .orElse(null);

        if (existingFriendship != null) {
            if (existingFriendship.isActive()) {
                throw new IllegalArgumentException("Friendship already exists.");
            } else {
                // Reactivate the friendship
                existingFriendship.setActive(true);
                friendRepository.save(existingFriendship);

                // Reactivate the reverse friendship
                Friend reverseFriendship = friendRepository.findByUserAndFriend(friend, user)
                        .orElseThrow(() -> new IllegalStateException("Reverse friendship not found."));
                reverseFriendship.setActive(true);
                friendRepository.save(reverseFriendship);
                return;
            }
        }

        // Create new friendships (bi-directional)
        friendRepository.save(Friend.builder().user(user).friend(friend).isActive(true).build());
        friendRepository.save(Friend.builder().user(friend).friend(user).isActive(true).build());
    }


    // 2. Get all friends for a user
    public List<User> getFriends(Long userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        return friendRepository.findByUserAndIsActiveTrue(user)
                .stream()
                .map(Friend::getFriend)
                .collect(Collectors.toList());
    }

    // 3. Remove a friend
    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("A user cannot remove themselves as a friend.");
        }

        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        User friend = userRepository.findByIdAndIsActiveTrue(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Friend not found."));

        // Check if the friendship exists
        boolean areFriends = friendRepository.existsByUserAndFriendAndIsActiveTrue(user, friend);
        if (!areFriends) {
            throw new IllegalArgumentException("The users are not friends.");
        }

        // Find and deactivate both friendship records
        friendRepository.findByUserAndFriendAndIsActiveTrue(user, friend)
                .ifPresent(f -> {
                    f.setActive(false);
                    friendRepository.save(f);
                });

        friendRepository.findByUserAndFriendAndIsActiveTrue(friend, user)
                .ifPresent(f -> {
                    f.setActive(false);
                    friendRepository.save(f);
                });
    }

}
