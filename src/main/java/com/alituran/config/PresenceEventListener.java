package com.alituran.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class PresenceEventListener {
/*
    private final ActiveUserStore activeUserStore;
    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) sha.getSessionAttributes().get("username");

        if (username != null) {
            activeUserStore.add(username);
            messagingTemplate.convertAndSend("/topic/users", activeUserStore.getUsers());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) sha.getSessionAttributes().get("username");

        if (username != null) {
            activeUserStore.remove(username);
            messagingTemplate.convertAndSend("/topic/users", activeUserStore.getUsers());
        }
    }*/
}
