package com.example.chatapp.controller;

import com.example.chatapp.model.Message;
import com.example.chatapp.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/channel/{channelId}")
    public ResponseEntity<Message> sendMessageToChannel(
            @PathVariable Long channelId,
            @RequestParam Long senderId,
            @Valid @RequestBody String content) {
        return ResponseEntity.ok(messageService.sendMessageToChannel(channelId, senderId, content));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<Page<Message>> getMessagesFromChannel(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(messageService.getMessagesFromChannel(channelId, page, size));
    }

    @PostMapping("/private")
    public ResponseEntity<Message> sendPrivateMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @Valid @RequestBody String content) {
        return ResponseEntity.ok(messageService.sendPrivateMessage(senderId, receiverId, content));
    }

    @GetMapping("/private")
    public ResponseEntity<Page<Message>> getPrivateMessages(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(messageService.getPrivateMessages(senderId, receiverId, page, size));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @Valid @RequestBody String newContent) {
        return ResponseEntity.ok(messageService.updateMessage(messageId, userId, newContent));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}
