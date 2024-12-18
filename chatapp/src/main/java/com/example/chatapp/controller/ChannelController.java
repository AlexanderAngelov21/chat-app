package com.example.chatapp.controller;

import com.example.chatapp.dto.CreateChannelRequest;
import com.example.chatapp.model.Channel;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.ChannelRepository;
import com.example.chatapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    // 1. Create a new channel

    @PostMapping
    public ResponseEntity<Channel> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        User owner = userRepository.findByIdAndIsActiveTrue(request.getOwnerId())
                .orElseThrow(() -> new NoSuchElementException("Owner with ID " + request.getOwnerId() + " not found."));

        Channel channel = Channel.builder()
                .name(request.getChannelName())
                .owner(owner)
                .isActive(true)
                .build();

        Channel savedChannel = channelRepository.save(channel);
        return new ResponseEntity<>(savedChannel, HttpStatus.CREATED);
    }


    // 2. List all active channels
    @GetMapping
    public ResponseEntity<List<Channel>> getAllActiveChannels() {
        List<Channel> channels = channelRepository.findByIsActiveTrue();
        return ResponseEntity.ok(channels);
    }

    // 3. Soft delete a channel (only owner can delete)
    @DeleteMapping("/{channelId}/owner/{ownerId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId, @PathVariable Long ownerId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("Channel not found."));

        if (!channel.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can delete this channel.");
        }

        channel.setActive(false);
        channelRepository.save(channel);
        return ResponseEntity.noContent().build();
    }

    // 4. List channels for a specific owner
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Channel>> getChannelsByOwner(@PathVariable Long ownerId) {
        List<Channel> channels = channelRepository.findByOwnerIdAndIsActiveTrue(ownerId);
        return ResponseEntity.ok(channels);
    }
}
