package com.alituran.controller;

import com.alituran.enums.MessageType;
import com.alituran.model.Message;
import com.alituran.model.User;
import com.alituran.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
public class ChatController {


  private final AuthRepository authRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/chat.sendMessage")  // /app/sendMessage ile gelir
    @SendTo("/topic/public")      // /topic/messages'e gönderilir
    public Message send(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username == null){
            throw new RuntimeException("User not logged in");
        }

        User user = authRepository.findByUsername(username).orElseThrow();

        if(user.isBanned()){
            throw new RuntimeException("You are banned from sending messages");
        }

        if(!user.isVerified()){
            throw new RuntimeException("You must verify your email before sending messages");
        }

        return message;
    }


    @MessageMapping("/chat.addUser")  // /app/sendMessage ile gelir
    @SendTo("/topic/public")
    public Message addUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        headerAccessor.setUser(() -> message.getSender());
        return message;
    }


    @MessageMapping("/privatemessage")
    public void sendPrivateMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== DM MESSAGE RECEIVED ===");
            System.out.println("Message receiver: " + (message != null ? message.getReceiver() : "null"));
            System.out.println("Message content: " + (message != null ? message.getContent() : "null"));
            System.out.println("Session attributes: " + headerAccessor.getSessionAttributes());

            String senderUsername = (String) headerAccessor.getSessionAttributes().get("username");
            System.out.println("Sender username from session: " + senderUsername);

            if(senderUsername == null) {
                System.err.println("ERROR: Username is null in session!");
                throw new RuntimeException("User not authenticated");
            }

            System.out.println("Step 1: Finding sender...");
            User sender = authRepository.findByUsername(senderUsername)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            System.out.println("Step 1: Sender found: " + sender.getUsername());

            System.out.println("Step 2: Checking if sender is banned...");
            if(sender.isBanned()){
                System.err.println("ERROR: Sender is banned!");
                throw new RuntimeException("You are banned from sending messages");
            }
            System.out.println("Step 2: Sender not banned");

            System.out.println("Step 3: Checking if sender is verified...");
            if(!sender.isVerified()){
                System.err.println("ERROR: Sender is not verified!");
                throw new RuntimeException("You must verify your email before sending messages");
            }
            System.out.println("Step 3: Sender is verified");

            System.out.println("Step 4: Finding receiver: " + message.getReceiver());
            User receiver = authRepository.findByUsername(message.getReceiver())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
            System.out.println("Step 4: Receiver found: " + receiver.getUsername());

            message.setSender(senderUsername);
            message.setMessageType(MessageType.PRIVATE);

            System.out.println("Step 5: Sending DM from: " + senderUsername + " to: " + receiver.getUsername());

            // Alıcıya gönder
            simpMessagingTemplate.convertAndSendToUser(
                    receiver.getUsername(),
                    "/queue/private",
                    message
            );

            System.out.println("Step 6: Message sent to receiver: " + receiver.getUsername());

            // BONUS: Gönderene de echo et (kendi mesajını görsün)
            simpMessagingTemplate.convertAndSendToUser(
                    senderUsername,
                    "/queue/private",
                    message
            );

            System.out.println("Step 7: Message echoed to sender: " + senderUsername);
            System.out.println("=== DM SENT SUCCESSFULLY ===");

        } catch (Exception e) {
            System.err.println("=== DM ERROR ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
