package com.mentalhealth.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * File Storage Configuration
 * Creates necessary directories for file uploads
 */
@Component
public class FileStorageConfig {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created upload directory: " + uploadDir);
            } else {
                System.err.println("Failed to create upload directory: " + uploadDir);
            }
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }
}

