package com.alituran.service;

import com.alituran.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp,application/pdf,text/plain}")
    private String allowedTypes;

    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        // Dosya boş mu kontrol et
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş olamaz");
        }



        // Dosya tipi kontrolü
        String contentType = file.getContentType();


        // Upload dizinini oluştur
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Benzersiz dosya adı oluştur
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Dosyayı kaydet
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // URL oluştur
        String fileUrl = "/api/files/" + uniqueFilename;

        return new FileUploadResponse(fileUrl, originalFilename, contentType);
    }

    public Resource getFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Dosya bulunamadı veya okunamıyor: " + filename);
        }

        return resource;
    }

    public MediaType getContentType(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            String contentType = Files.probeContentType(filePath);
            return MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream");
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}

