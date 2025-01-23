package com.example.chatapp.service;

import com.example.chatapp.dto.CreateChannelRequest;
import com.example.chatapp.model.Channel;
import com.example.chatapp.model.ChannelMember;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.ChannelMemberRepository;
import com.example.chatapp.repository.ChannelRepository;
import com.example.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ChannelMemberRepository channelMemberRepository;

    public Channel createChannel(CreateChannelRequest request) {
        User owner = userRepository.findByIdAndIsActiveTrue(request.getOwnerId())
                .orElseThrow(() -> new NoSuchElementException("Owner with ID " + request.getOwnerId() + " not found."));

        Channel channel = Channel.builder()
                .name(request.getChannelName())
                .owner(owner)
                .isActive(true)
                .build();

        Channel savedChannel = channelRepository.save(channel);

        ChannelMember ownerMember = ChannelMember.builder()
                .channel(savedChannel)
                .user(owner)
                .role("OWNER")
                .isActive(true)
                .build();

        channelMemberRepository.save(ownerMember);

        return savedChannel;
    }

    public List<Channel> getAllActiveChannels() {
        return channelRepository.findByIsActiveTrue();
    }

    public void deleteChannel(Long channelId, Long ownerId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("Channel not found."));

        if (!channel.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can delete this channel.");
        }

        channel.setActive(false);
        channelRepository.save(channel);
    }

    public List<Channel> getChannelsByOwner(Long ownerId) {
        return channelRepository.findByOwnerIdAndIsActiveTrue(ownerId);
    }

    public String addUserToChannel(Long channelId, Long actorId, Long userId) {
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        boolean isOwner = channel.getOwner().getId().equals(actorId);
        boolean isAdmin = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, actorId)
                .map(member -> member.getRole().equals("ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can add members.");
        }

        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found or is inactive."));

        if (channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this channel.");
        }

        ChannelMember member = ChannelMember.builder()
                .channel(channel)
                .user(user)
                .role("MEMBER")
                .isActive(true)
                .build();

        channelMemberRepository.save(member);
        return "User added to the channel successfully.";
    }

    public String assignAdminRole(Long channelId, Long ownerId, Long userId) {
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        if (!channel.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can assign ADMIN roles.");
        }

        ChannelMember member = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId)
                .orElseThrow(() -> new NoSuchElementException("User is not a member of this channel."));

        member.setRole("ADMIN");
        channelMemberRepository.save(member);

        return "User assigned as ADMIN successfully.";
    }

    public Channel updateChannel(Long channelId, Long userId, String newName) {
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel with ID " + channelId + " not found."));

        boolean isOwner = channel.getOwner().getId().equals(userId);
        boolean isAdmin = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId)
                .map(member -> member.getRole().equals("ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can update this channel.");
        }

        channel.setName(newName);
        return channelRepository.save(channel);
    }

    public List<Map<String, Object>> listChannelMembers(Long channelId) {
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        List<ChannelMember> members = channelMemberRepository.findByChannelIdAndIsActiveTrue(channelId);

        return members.stream()
                .map(member -> {
                    Map<String, Object> memberDetail = new HashMap<>();
                    memberDetail.put("userId", member.getUser().getId());
                    memberDetail.put("username", member.getUser().getUsername());
                    memberDetail.put("email", member.getUser().getEmail());
                    memberDetail.put("role", member.getRole());
                    return memberDetail;
                })
                .toList();
    }

    public String removeMemberFromChannel(Long channelId, Long actorId, Long userId) {
        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        boolean isOwner = channel.getOwner().getId().equals(actorId);
        boolean isAdmin = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, actorId)
                .map(member -> member.getRole().equals("ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can remove members.");
        }

        ChannelMember member = channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId)
                .orElseThrow(() -> new NoSuchElementException("User is not a member of this channel."));

        if (member.getRole().equals("ADMIN") && !isOwner) {
            throw new IllegalArgumentException("Only the owner can remove an admin.");
        }

        member.setActive(false);
        channelMemberRepository.save(member);

        return "User removed from the channel successfully.";
    }
    public List<Channel> getUserChannels(Long userId) {
        return channelMemberRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(ChannelMember::getChannel)
                .filter(Channel::isActive)
                .toList();
    }
    public String getUserRoleInChannel(Long channelId, Long userId) {
        return channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(channelId, userId)
                .map(ChannelMember::getRole)
                .orElseThrow(() -> new NoSuchElementException("User is not a member of this channel."));
    }


}
