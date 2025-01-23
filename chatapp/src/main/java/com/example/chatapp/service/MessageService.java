package com.example.chatapp.service;

import com.example.chatapp.model.Channel;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final FriendRepository friendRepository;

    public Message sendMessageToChannel(Long channelId, Long senderId, String content) {
        User sender = userRepository.findByIdAndIsActiveTrue(senderId)
                .filter(User::isActive)
                .orElseThrow(() -> new NoSuchElementException("Sender not found."));

        Channel channel = channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        Message message = Message.builder()
                .channel(channel)
                .sender(sender)
                .content(content)
                .isActive(true)
                .build();

        return messageRepository.save(message);
    }

    public Page<Message> getMessagesFromChannel(Long channelId, int page, int size) {
        channelRepository.findById(channelId)
                .filter(Channel::isActive)
                .orElseThrow(() -> new NoSuchElementException("Channel not found or is inactive."));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return messageRepository.findByChannelIdAndIsActiveTrue(channelId, pageable);
    }

    public Message sendPrivateMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findByIdAndIsActiveTrue(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found."));
        User receiver = userRepository.findByIdAndIsActiveTrue(receiverId)
                .orElseThrow(() -> new NoSuchElementException("Receiver not found."));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isActive(true)
                .build();

        return messageRepository.save(message);
    }

    public Page<Message> getPrivateMessages(Long senderId, Long receiverId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return messageRepository.findBySenderIdAndReceiverIdAndIsActiveTrue(senderId, receiverId, pageable);
    }

    public Message updateMessage(Long messageId, Long userId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found."));

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

        message.setContent(newContent);
        return messageRepository.save(message);
    }

    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found."));

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

        message.setActive(false);
        messageRepository.save(message);
    }
    public Message sendMessageToFriend(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Users cannot send messages to themselves.");
        }
        User sender = userRepository.findByIdAndIsActiveTrue(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found."));
        User receiver = userRepository.findByIdAndIsActiveTrue(receiverId)
                .orElseThrow(() -> new NoSuchElementException("Receiver not found."));

        // Validate friendship
        if (!friendRepository.existsByUserAndFriendAndIsActiveTrue(sender, receiver)) {
            throw new IllegalArgumentException("Sender and receiver are not friends.");
        }

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isActive(true)
                .build();

        return messageRepository.save(message);
    }
    public Page<Message> getFriendMessages(Long senderId, Long receiverId, int page, int size) {
        User sender = userRepository.findByIdAndIsActiveTrue(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found."));
        User receiver = userRepository.findByIdAndIsActiveTrue(receiverId)
                .orElseThrow(() -> new NoSuchElementException("Receiver not found."));

        // Validate friendship
        if (!friendRepository.existsByUserAndFriendAndIsActiveTrue(sender, receiver)) {
            throw new IllegalArgumentException("Sender and receiver are not friends.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return messageRepository.findMessagesBetweenUsers(senderId, receiverId, pageable);
    }
    public Message updateFriendMessage(Long messageId, Long senderId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found."));

        // Only the sender can update the message
        if (!message.getSender().getId().equals(senderId)) {
            throw new IllegalArgumentException("You do not have permission to update this message.");
        }

        message.setContent(newContent);
        return messageRepository.save(message);
    }
    public void deleteFriendMessage(Long messageId, Long senderId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found."));

        // Only the sender can delete the message
        if (!message.getSender().getId().equals(senderId)) {
            throw new IllegalArgumentException("You do not have permission to delete this message.");
        }

        // Soft delete the message
        message.setActive(false);
        messageRepository.save(message);
    }
}
