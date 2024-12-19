package com.example.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {
    @NotNull(message = "Sender ID is required")
    private Long senderId;

    private Long receiverId; // Optional for channel messages

    @NotBlank(message = "Message content is required")
    private String content;
}

