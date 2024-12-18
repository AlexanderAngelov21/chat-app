package com.example.chatapp.controller;

import com.example.chatapp.model.Channel;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.ChannelMemberRepository;
import com.example.chatapp.repository.ChannelRepository;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    // 1. Send a message to a channel
    @PostMapping("/channel/{channelId}")
    public ResponseEntity<Message> sendMessageToChannel(
            @PathVariable Long channelId,
            @RequestParam Long senderId,
            @Valid @RequestBody String content) {

        // Validate sender
        User sender = userRepository.findByIdAndIsActiveTrue(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found."));

        // Validate channel
        channelRepository.findById(channelId)
                .filter(channel -> channel.isActive())
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        // Save the message
        Message message = Message.builder()
                .channel(channelRepository.findById(channelId).orElseThrow())
                .sender(sender)
                .content(content)
                .isActive(true)
                .build();

        return new ResponseEntity<>(messageRepository.save(message), HttpStatus.CREATED);
    }

    // 2. Retrieve messages from a channel with pagination
    @GetMapping("/channel/{channelId}")
    public ResponseEntity<Page<Message>> getMessagesFromChannel(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validate channel
        channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Message> messages = messageRepository.findByChannelIdAndIsActiveTrue(channelId, pageable);

        return ResponseEntity.ok(messages);
    }

    // 3. Send a private message
    @PostMapping("/private")
    public ResponseEntity<Message> sendPrivateMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @Valid @RequestBody String content) {

        // Validate sender and receiver
        User sender = userRepository.findByIdAndIsActiveTrue(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found."));
        User receiver = userRepository.findByIdAndIsActiveTrue(receiverId)
                .orElseThrow(() -> new NoSuchElementException("Receiver not found."));

        // Save the message
        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isActive(true)
                .build();

        return new ResponseEntity<>(messageRepository.save(message), HttpStatus.CREATED);
    }

    // 4. Retrieve private messages with pagination
    @GetMapping("/private")
    public ResponseEntity<Page<Message>> getPrivateMessages(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Message> messages = messageRepository.findBySenderIdAndReceiverIdAndIsActiveTrue(senderId, receiverId, pageable);

        return ResponseEntity.ok(messages);
    }
    // 5. Update a message
    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @Valid @RequestBody String newContent) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found."));

        // Validate permissions
        boolean canUpdate =
                (message.getReceiver() != null && message.getSender().getId().equals(userId)) || // Private message
                        (message.getChannel() != null &&
                                (message.getSender().getId().equals(userId) || // Channel message by sender
                                        channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(message.getChannel().getId(), userId)
                                                .map(member -> member.getRole().equals("ADMIN"))
                                                .orElse(false) || // Admin check
                                        message.getChannel().getOwner().getId().equals(userId))); // Owner check

        if (!canUpdate) {
            throw new IllegalArgumentException("You do not have permission to update this message.");
        }

        // Update the message content
        message.setContent(newContent);
        return ResponseEntity.ok(messageRepository.save(message));
    }
    // 6. Soft delete a message
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found."));

        // Validate permissions
        boolean canDelete =
                (message.getReceiver() != null && message.getSender().getId().equals(userId)) || // Private message
                        (message.getChannel() != null &&
                                (message.getSender().getId().equals(userId) || // Channel message by sender
                                        channelMemberRepository.findByChannelIdAndUserIdAndIsActiveTrue(message.getChannel().getId(), userId)
                                                .map(member -> member.getRole().equals("ADMIN"))
                                                .orElse(false) || // Admin check
                                        message.getChannel().getOwner().getId().equals(userId))); // Owner check

        if (!canDelete) {
            throw new IllegalArgumentException("You do not have permission to delete this message.");
        }

        // Soft delete the message
        message.setActive(false);
        messageRepository.save(message);

        return ResponseEntity.noContent().build();
    }

}
