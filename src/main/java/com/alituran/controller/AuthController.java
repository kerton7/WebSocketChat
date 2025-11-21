package com.alituran.controller;

import com.alituran.dto.AuthRequest;
import com.alituran.dto.AuthResponse;
import com.alituran.model.User;
import com.alituran.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthRequest> register(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.register(authRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }

    @PostMapping("/admin/ban/{id}")
    public ResponseEntity<String> banUser(@PathVariable Long id) throws IOException {
        return ResponseEntity.ok(authService.banUser(id));
    }

    @PostMapping("/admin/unban/{id}")
    public ResponseEntity<String> unBanUser(@PathVariable Long id) throws IOException {
        return ResponseEntity.ok(authService.unBanUser(id));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/logout/{username}")
    public ResponseEntity<String> logout(@PathVariable String username) {
        return ResponseEntity.ok(authService.logout(username));
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String code) {
        try {
            authService.verifyUser(code);
            return ResponseEntity.ok("Email doğrulandı! Artık mesaj gönderebilirsin.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Geçersiz doğrulama kodu!");
        }
    }
}
