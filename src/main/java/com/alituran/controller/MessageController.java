package com.alituran.controller;

import com.alituran.enums.MessageType;
import com.alituran.model.Message;
import com.alituran.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;

    @GetMapping("/history")
    public ResponseEntity<List<Message>> getPublicChatHistory(
            @RequestParam(defaultValue = "50") int limit) {
        List<Message> messages = messageRepository.findByMessageTypeOrderByTimestampDesc(
                MessageType.CHAT,
                PageRequest.of(0, limit)
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/dm/{username}")
    public ResponseEntity<List<Message>> getPrivateMessageHistory(
            @PathVariable String username,
            @RequestParam String currentUser) {
        List<Message> messages = messageRepository.findPrivateMessagesBetweenUsers(
                currentUser,
                username
        );
        return ResponseEntity.ok(messages);
    }
}

