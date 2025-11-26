package com.alituran.config;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveUserStore {
    private final Set<String> users = ConcurrentHashMap.newKeySet();

    public void add(String username) {
        users.add(username);
    }

    public void remove(String username) {
        users.remove(username);
    }

    public Set<String> getUsers() {
        return Set.copyOf(users);
    }
}
