package com.alituran.controller;

import com.alituran.config.ActiveUserStore;
import com.alituran.enums.MessageType;
import com.alituran.model.Message;
import com.alituran.model.User;
import com.alituran.repository.AuthRepository;
import com.alituran.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
public class ChatController {


  private final AuthRepository authRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ActiveUserStore activeUserStore;

    private final MessageRepository messageRepository;


    @MessageMapping("/chat.sendMessage")  // /app/sendMessage ile gelir
    @SendTo("/topic/public")      // /topic/messages'e gönderilir
    public Message send(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username == null){
            throw new RuntimeException("User not logged in");
        }

        User user = authRepository.findByUsername(username).orElseThrow();

        if(user.isBanned()){
            // Kullanıcıya özel hata mesajı gönder
            Message errorMessage = Message.builder()
                    .sender("System")
                    .content("Hesabınız yasaklandı! Mesaj gönderemezsiniz.")
                    .messageType(MessageType.CHAT)
                    .timestamp(LocalDateTime.now())
                    .build();
            simpMessagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/errors",
                    errorMessage
            );
            throw new RuntimeException("You are banned from sending messages");
        }

        if(!user.isVerified()){
            // Kullanıcıya özel hata mesajı gönder
            Message errorMessage = Message.builder()
                    .sender("System")
                    .content("Email adresinizi doğrulamanız gerekiyor! Mesaj göndermek için email doğrulama linkine tıklayın.")
                    .messageType(MessageType.CHAT)
                    .timestamp(LocalDateTime.now())
                    .build();
            simpMessagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/errors",
                    errorMessage
            );
            throw new RuntimeException("You must verify your email before sending messages");
        }

        // Mesajı veritabanına kaydet
        message.setSender(username);
        message.setMessageType(MessageType.CHAT);
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);

        return message;
    }


    @MessageMapping("/chat.addUser")  // /app/sendMessage ile gelir
    @SendTo("/topic/public")
    public Message addUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        String username = message.getSender();
        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.setUser(() -> username);
        
        // Aktif kullanıcı listesine ekle ve güncelle
        activeUserStore.add(username);
        simpMessagingTemplate.convertAndSend("/topic/activeUsers", activeUserStore.getUsers());
        
        return message;
    }


    @MessageMapping("/privatemessage")
    public void sendPrivateMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        try {

            String senderUsername = (String) headerAccessor.getSessionAttributes().get("username");


            if(senderUsername == null) {
                throw new RuntimeException("User not authenticated");
            }
            User sender = authRepository.findByUsername(senderUsername)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            if(sender.isBanned()){
                // Kullanıcıya özel hata mesajı gönder
                Message errorMessage = Message.builder()
                        .sender("System")
                        .content("Hesabınız yasaklandı! Mesaj gönderemezsiniz.")
                        .messageType(MessageType.PRIVATE)
                        .timestamp(LocalDateTime.now())
                        .build();
                simpMessagingTemplate.convertAndSendToUser(
                        senderUsername,
                        "/queue/errors",
                        errorMessage
                );
                throw new RuntimeException("You are banned from sending messages");
            }
            if(!sender.isVerified()){
                // Kullanıcıya özel hata mesajı gönder
                Message errorMessage = Message.builder()
                        .sender("System")
                        .content("Email adresinizi doğrulamanız gerekiyor! Mesaj göndermek için email doğrulama linkine tıklayın.")
                        .messageType(MessageType.PRIVATE)
                        .timestamp(LocalDateTime.now())
                        .build();
                simpMessagingTemplate.convertAndSendToUser(
                        senderUsername,
                        "/queue/errors",
                        errorMessage
                );
                throw new RuntimeException("You must verify your email before sending messages");
            }
            User receiver = authRepository.findByUsername(message.getReceiver())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));

            message.setSender(senderUsername);
            message.setMessageType(MessageType.PRIVATE);
            message.setTimestamp(LocalDateTime.now());

            // Mesajı veritabanına kaydet
            messageRepository.save(message);

            // Alıcıya gönder
            simpMessagingTemplate.convertAndSendToUser(
                    receiver.getUsername(),
                    "/queue/private",
                    message
            );


            // BONUS: Gönderene de echo et (kendi mesajını görsün)
            simpMessagingTemplate.convertAndSendToUser(
                    senderUsername,
                    "/queue/private",
                    message
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
