package com.alituran.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final Map<Long, String> activeJwtSessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void remove(Long userId) {
        sessions.remove(userId);
    }

    public void closeSession(Long userId) throws IOException {
        if (sessions.containsKey(userId)) {
            sessions.get(userId).close();
            sessions.remove(userId);
        }
    }

    public boolean isAnyUserLoggedIn() {
        return !activeJwtSessions.isEmpty();
    }

    public void registerLogin(Long userId, String jwtToken) {
        activeJwtSessions.put(userId, jwtToken);
    }

    public void removeLogin(Long userId) {
        activeJwtSessions.remove(userId);
    }

    public boolean isUserLoggedIn(Long userId) {
        return activeJwtSessions.containsKey(userId);
    }

    public Map<Long, String> getActiveJwtSessions() {
        return activeJwtSessions;
    }


}
