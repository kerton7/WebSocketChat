package com.alituran.config;

import com.alituran.jwt.JwtService;
import com.alituran.model.User;
import com.alituran.repository.AuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final SessionRegistry sessionRegistry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        // Google'dan gelen bilgileri al
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String sub = oauth2User.getAttribute("sub"); // Google user ID
        
        // Email veya name'den username oluştur
        String username = email != null ? email.split("@")[0] : (name != null ? name.replaceAll("\\s+", "").toLowerCase() : sub);
        
        // Kullanıcıyı veritabanında kontrol et veya oluştur
        Optional<User> existingUser = authRepository.findByUsername(username);
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Email ile de kontrol et
            Optional<User> userByEmail = email != null ? authRepository.findByEmail(email) : Optional.empty();
            
            if (userByEmail.isPresent()) {
                user = userByEmail.get();
            } else {
                // Yeni kullanıcı oluştur
                user = User.builder()
                        .username(username)
                        .email(email != null ? email : username + "@google.com")
                        .password(UUID.randomUUID().toString()) // OAuth kullanıcıları için random password
                        .role("ROLE_USER")
                        .verified(true) // Google ile gelen kullanıcılar otomatik verified
                        .banned(false)
                        .build();
                authRepository.save(user);
            }
        }
        
        // Ban kontrolü
        if (user.isBanned()) {
            response.sendRedirect("/index.html?error=banned");
            return;
        }
        
        // JWT token oluştur
        String accessToken = jwtService.generateToken(user);
        user.setCurrentToken(accessToken);
        authRepository.save(user);
        sessionRegistry.registerLogin(user.getId(), accessToken);
        
        // Frontend'e token ile yönlendir (URL encode et)
        String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String encodedUsername = URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8);
        String encodedRole = URLEncoder.encode(user.getRole(), StandardCharsets.UTF_8);
        
        // Relative URL kullan (daha güvenilir)
        String redirectUrl = "/chat.html?token=" + encodedToken + "&username=" + encodedUsername + "&role=" + encodedRole;
        
        System.out.println("OAuth2 redirect URL: " + redirectUrl);
        System.out.println("Token length: " + accessToken.length());
        System.out.println("Username: " + user.getUsername());
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

