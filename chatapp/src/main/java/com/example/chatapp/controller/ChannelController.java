package com.example.chatapp.controller;

import com.example.chatapp.dto.CreateChannelRequest;
import com.example.chatapp.model.Channel;
import com.example.chatapp.model.ChannelMember;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.ChannelMemberRepository;
import com.example.chatapp.repository.ChannelRepository;
import com.example.chatapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ChannelMemberRepository channelMemberRepository;
    // 1. Create a new channel

    @PostMapping
    public ResponseEntity<Channel> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        User owner = userRepository.findByIdAndIsActiveTrue(request.getOwnerId())
                .orElseThrow(() -> new NoSuchElementException("Owner with ID " + request.getOwnerId() + " not found."));

        // Create the channel
        Channel channel = Channel.builder()
                .name(request.getChannelName())
                .owner(owner)
                .isActive(true)
                .build();
        Channel savedChannel = channelRepository.save(channel);

        // Add owner to ChannelMember table with role OWNER
        ChannelMember ownerMember = ChannelMember.builder()
                .channel(savedChannel)
                .user(owner)
                .role("OWNER")
                .isActive(true)
                .build();
        channelMemberRepository.save(ownerMember);

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
    // 5. Add a user to a channel
    @PostMapping("/{channelId}/addUser")
    public ResponseEntity<String> addUserToChannel(
            @PathVariable Long channelId,
            @RequestParam Long actorId,
            @RequestParam Long userId) {

        // Validate that the channel is active
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        // Validate that the actor is authorized (owner or admin)
        boolean isOwner = channel.getOwner().getId().equals(actorId);
        boolean isAdmin = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, actorId)
                .map(member -> member.getRole().equals("ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the channel owner or an admin can add members to this channel.");
        }

        // Validate the user to be added
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found or is inactive."));

        // Check if the user is already a member
        if (channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this channel.");
        }

        // Add user as a MEMBER
        ChannelMember member = ChannelMember.builder()
                .channel(channel)
                .user(user)
                .role("MEMBER")
                .isActive(true)
                .build();

        channelMemberRepository.save(member);
        return ResponseEntity.ok("User added to the channel successfully.");
    }

    // 6. Assign ADMIN role
    @PutMapping("/{channelId}/assignAdmin")
    public ResponseEntity<String> assignAdminRole(
            @PathVariable Long channelId,
            @RequestParam Long ownerId,
            @RequestParam Long userId) {

        // Validate that the channel is active
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));
        // Validate that the user is the owner
        if (!channel.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can assign ADMIN roles.");
        }

        // Assign ADMIN role
        ChannelMember member = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId)
                .orElseThrow(() -> new NoSuchElementException("User is not a member of this channel."));
        member.setRole("ADMIN");
        channelMemberRepository.save(member);

        return ResponseEntity.ok("User assigned as ADMIN successfully.");
    }
    // 7. Update channel details (only owner or admin can update)
    @PutMapping("/{channelId}/user/{userId}")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable Long channelId,
            @PathVariable Long userId,
            @RequestParam String newName) {

        Channel channel = channelRepository.findById(channelId).filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel with ID " + channelId + " not found."));

        // Check if the user is the owner or an admin
        boolean isOwner = channel.getOwner().getId().equals(userId);
        boolean isAdmin = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId)
                .map(member -> member.getRole().equals("ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can update this channel.");
        }

        // Update the channel name
        channel.setName(newName);
        Channel updatedChannel = channelRepository.save(channel);

        return ResponseEntity.ok(updatedChannel);
    }
    // 8. List all members of a channel
    @GetMapping("/{channelId}/members")
    public ResponseEntity<List<Map<String, Object>>> listChannelMembers(@PathVariable Long channelId) {
        // Validate if the channel is active
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        // Fetch active members of the channel
        List<ChannelMember> members = channelMemberRepository.findByChannelIdAndIsActiveTrue(channelId);

        if (members.isEmpty()) {
            throw new NoSuchElementException("No members found for the given channel.");
        }

        // Transform to required response format
        List<Map<String, Object>> memberDetails = members.stream()
                .map(member -> {
                    Map<String, Object> memberDetail = new HashMap<>();
                    memberDetail.put("userId", member.getUser().getId());
                    memberDetail.put("username", member.getUser().getUsername());
                    memberDetail.put("email", member.getUser().getEmail());
                    memberDetail.put("role", member.getRole());
                    return memberDetail;
                })
                .toList();

        return ResponseEntity.ok(memberDetails);
    }
    // 9. Remove a user from a channel (only owner can remove)
    @DeleteMapping("/{channelId}/removeUser")
    public ResponseEntity<String> removeMemberFromChannel(
            @PathVariable Long channelId,
            @RequestParam Long actorId,
            @RequestParam Long userId) {

        // Validate channel
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        // Validate actor (owner or admin)
        boolean isOwner = channel.getOwner().getId().equals(actorId);
        boolean isAdmin = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, actorId)
                .map(member -> member.getRole().equals("ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the channel owner or an admin can remove members.");
        }

        // Validate member to remove
        ChannelMember member = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId)
                .orElseThrow(() -> new NoSuchElementException("User is not a member of this channel."));

        // Only owner can remove an admin
        if (member.getRole().equals("ADMIN") && !isOwner) {
            throw new IllegalArgumentException("Only the channel owner can remove an admin.");
        }

        // Soft delete the member
        member.setActive(false);
        channelMemberRepository.save(member);

        return ResponseEntity.ok("User removed from the channel successfully.");
    }

}
