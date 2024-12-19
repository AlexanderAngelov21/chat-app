package com.example.chatapp.controller;

import com.example.chatapp.dto.CreateChannelRequest;
import com.example.chatapp.model.Channel;
import com.example.chatapp.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<Channel> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        return ResponseEntity.ok(channelService.createChannel(request));
    }

    @GetMapping
    public ResponseEntity<List<Channel>> getAllActiveChannels() {
        return ResponseEntity.ok(channelService.getAllActiveChannels());
    }

    @DeleteMapping("/{channelId}/owner/{ownerId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId, @PathVariable Long ownerId) {
        channelService.deleteChannel(channelId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Channel>> getChannelsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(channelService.getChannelsByOwner(ownerId));
    }

    @PostMapping("/{channelId}/addUser")
    public ResponseEntity<String> addUserToChannel(@PathVariable Long channelId, @RequestParam Long actorId, @RequestParam Long userId) {
        return ResponseEntity.ok(channelService.addUserToChannel(channelId, actorId, userId));
    }

    @PutMapping("/{channelId}/assignAdmin")
    public ResponseEntity<String> assignAdminRole(@PathVariable Long channelId, @RequestParam Long ownerId, @RequestParam Long userId) {
        return ResponseEntity.ok(channelService.assignAdminRole(channelId, ownerId, userId));
    }

    @PutMapping("/{channelId}/user/{userId}")
    public ResponseEntity<Channel> updateChannel(@PathVariable Long channelId, @PathVariable Long userId, @RequestParam String newName) {
        return ResponseEntity.ok(channelService.updateChannel(channelId, userId, newName));
    }

    @GetMapping("/{channelId}/members")
    public ResponseEntity<List<Map<String, Object>>> listChannelMembers(@PathVariable Long channelId) {
        return ResponseEntity.ok(channelService.listChannelMembers(channelId));
    }

    @DeleteMapping("/{channelId}/removeUser")
    public ResponseEntity<String> removeMemberFromChannel(@PathVariable Long channelId, @RequestParam Long actorId, @RequestParam Long userId) {
        return ResponseEntity.ok(channelService.removeMemberFromChannel(channelId, actorId, userId));
    }
}
