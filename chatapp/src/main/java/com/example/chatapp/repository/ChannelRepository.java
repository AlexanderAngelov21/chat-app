package com.example.chatapp.repository;

import com.example.chatapp.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByIsActiveTrue();
    List<Channel> findByOwnerIdAndIsActiveTrue(Long ownerId);
}
