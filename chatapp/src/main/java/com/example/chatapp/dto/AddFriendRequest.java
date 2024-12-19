package com.example.chatapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFriendRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotNull(message = "Friend ID is required")
    private Long friendId;
}