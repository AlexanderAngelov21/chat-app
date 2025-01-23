package com.example.chatapp.repository;

import com.example.chatapp.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    Page<Message> findByChannelIdAndIsActiveTrue(Long channelId, Pageable pageable);
    Page<Message> findBySenderIdAndReceiverIdAndIsActiveTrue(Long senderId, Long receiverId, Pageable pageable);
    @Query("SELECT m FROM Message m " +
            "WHERE ((m.sender.id = :userId AND m.receiver.id = :friendId) " +
            "   OR (m.sender.id = :friendId AND m.receiver.id = :userId)) " +
            "AND m.isActive = true " +
            "ORDER BY m.createdAt ASC")
    Page<Message> findMessagesBetweenUsers(@Param("userId") Long userId,
                                           @Param("friendId") Long friendId,
                                           Pageable pageable);
}
