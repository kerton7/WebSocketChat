package com.alituran.service;

import com.alituran.config.SessionRegistry;
import com.alituran.dto.AuthRequest;
import com.alituran.dto.AuthResponse;
import com.alituran.jwt.JwtService;
import com.alituran.model.User;
import com.alituran.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final AuthRepository authRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AuthenticationProvider authenticationProvider;

    private final JwtService jwtService;

    private final SessionRegistry sessionRegistry;

    private final JavaMailSender javaMailSender;

    public AuthRequest register(AuthRequest authRequest) {
        User user = User.builder().username(authRequest.username()).
                password(bCryptPasswordEncoder.encode(authRequest.password()))
                .email(authRequest.email())
                .verificationCode(UUID.randomUUID().toString()).verified(false)
                .role("ROLE_USER").build();

        authRepository.save(user);
        sendVerificationEmail(user);
        return authRequest;
    }

    private void sendVerificationEmail(User user) {
        String link = "https://august-redissoluble-cristian.ngrok-free.dev/auth/verify?code=" + user.getVerificationCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Email Doğrulama");
        message.setText("Emailinizi doğrulamak için tıklayın: " + link);

        javaMailSender.send(message);
    }
    public void verifyUser(String code) {
        User user = authRepository.findByVerificationCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz doğrulama kodu"));

        user.setVerified(true);
        user.setVerificationCode(null); // Kod artık kullanılamaz
        authRepository.save(user);
    }


    public AuthResponse authenticate(AuthRequest authRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password());

        User user = authRepository.findByUsername(
                authRequest.username()).orElseThrow(() -> new UsernameNotFoundException("Username not found!"));
        String accessToken = jwtService.generateToken(user);

        if(user.isBanned()){
            throw new RuntimeException("User is banned!");
        }
        user.setCurrentToken(accessToken);
        authRepository.save(user);
        authenticationProvider.authenticate(authenticationToken);
        sessionRegistry.registerLogin(user.getId(), accessToken);
        return new AuthResponse(accessToken,user.getRole());
    }

    public String logout(String username) {
        User user = authRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setCurrentToken(null);
        authRepository.save(user);
        sessionRegistry.removeLogin(user.getId());
        return "Logged out";
    }


    public String banUser(Long id) throws IOException {
       var user = authRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Username not found!"));
       user.setBanned(true);
       authRepository.save(user);
       sessionRegistry.closeSession(id);
       return "User banned";
    }

    public String unBanUser(Long id) {
        var user = authRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Username not found!"));
        if(!user.isBanned())  return "User is already not banned";

        user.setBanned(false);
        authRepository.save(user);

        return "User unbanned";
    }

    public List<User> getAllUsers() {
        return authRepository.findAll();
    }





}

