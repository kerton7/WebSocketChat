package com.alituran.jwt;

import com.alituran.jwt.JwtService;
import com.alituran.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
        String query = uri.getQuery();   // token=....

        if (query == null || !query.startsWith("token=")) {
            return false;
        }

        String token = query.substring(6);

        String username = jwtService.getUsernameFromToken(token);
        UserDetails user = userDetailsService.loadUserByUsername(username);

        if (user instanceof User userr) {
            if (userr.isBanned()) {
                System.out.println("Banned user tried to connect: " + username);
                return false;
            }
        }

        attributes.put("username", username);
        attributes.put("roles", user.getAuthorities());

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
