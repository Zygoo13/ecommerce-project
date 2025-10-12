package com.zygoo13.admin;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class FileUploadUtil {
    public static void saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path target = uploadPath.resolve(fileName).normalize();

            // Chặn path traversal
            if (!target.startsWith(uploadPath)) {
                throw new SecurityException("Invalid file path.");
            }

            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Could not save file: " + fileName, e);
        }
    }

    public static void cleanDir(String dir) {
        Path dirPath = Paths.get(dir);
        try {
            if (!Files.exists(dirPath)) return;

            Files.list(dirPath).forEach(file -> {
                if (!Files.isDirectory(file)) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        System.out.println("Could not delete file: " + file);
                    }
                }
            });
        } catch (IOException e) {
            System.out.println("Could not list directory: " + dirPath);
        }
    }
}
