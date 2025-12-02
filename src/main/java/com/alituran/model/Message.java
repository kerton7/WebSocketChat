package com.alituran.model;


import com.alituran.enums.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    private String receiver;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String fileUrl; // Yüklenen dosyanın URL'i
    
    private String fileName; // Dosya adı
    
    private String fileType; // Dosya tipi (image/jpeg, image/png, application/pdf, vb.)

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
