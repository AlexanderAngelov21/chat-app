package com.example.chatapp.controller;

import com.example.chatapp.dto.AddFriendRequest;
import com.example.chatapp.model.User;
import com.example.chatapp.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // 1. Add a friend
    @PostMapping
    public ResponseEntity<String> addFriend(@RequestBody AddFriendRequest request) {
        friendService.addFriend(request.getUserId(), request.getFriendId());
        return ResponseEntity.ok("Friend added successfully.");
    }

    // 2. Get all friends for a user
    @GetMapping("/{userId}")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    // 3. Remove a friend
    @DeleteMapping
    public ResponseEntity<String> removeFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok("Friend removed successfully.");
    }
}
