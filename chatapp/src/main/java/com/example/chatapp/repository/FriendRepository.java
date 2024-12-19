package com.example.chatapp.repository;

import com.example.chatapp.model.Friend;
import com.example.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserAndIsActiveTrue(User user);

    Optional<Friend> findByUserAndFriendAndIsActiveTrue(User user, User friend);
    Optional<Friend> findByUserAndFriend(User user, User friend);

    boolean existsByUserAndFriendAndIsActiveTrue(User user, User friend);
}
