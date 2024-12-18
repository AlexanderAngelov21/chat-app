package com.example.chatapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "channel_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role; // OWNER, ADMIN, MEMBER

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
