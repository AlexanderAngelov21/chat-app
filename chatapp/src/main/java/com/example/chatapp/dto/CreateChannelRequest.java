package com.example.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateChannelRequest {

    @NotBlank(message = "Channel name is required")
    private String channelName;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;
}
