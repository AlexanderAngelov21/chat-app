package com.example.chatapp.repository;

import com.example.chatapp.model.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {
    List<ChannelMember> findByChannelIdAndIsActiveTrue(Long channelId);
    List<ChannelMember> findByUserIdAndIsActiveTrue(Long userId);
    Optional<ChannelMember> findByChannelIdAndUserIdAndIsActiveTrue(Long channelId, Long userId);
}
