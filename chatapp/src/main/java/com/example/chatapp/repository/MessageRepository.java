package com.example.chatapp.repository;

import com.example.chatapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Fetch all messages in a channel
    List<Message> findByChannelIdAndIsActiveTrueOrderByCreatedAtAsc(Long channelId);

    // Fetch all private messages between two users
    List<Message> findBySenderIdAndReceiverIdAndIsActiveTrueOrderByCreatedAtAsc(Long senderId, Long receiverId);

    // Fetch messages sent by a specific user in a channel
    List<Message> findByChannelIdAndSenderIdAndIsActiveTrueOrderByCreatedAtAsc(Long channelId, Long senderId);
}
