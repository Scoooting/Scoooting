package org.scoooting.files.services;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private void checkDirectory(Path path) throws IOException {
        if (!Files.exists(path))
            Files.createDirectories(path);
    }

//    public Mono<Void> uploadPhoto(Long id, FilePart file) throws IOException {
//        String fullPath = photosPath + "/" + id;
//        checkDirectory(Path.of(fullPath));
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
//        return file.transferTo(Path.of(fullPath + "/" + formatter.format(LocalDateTime.now())));
//    }

    public void uploadPhoto(Long id, MultipartFile file) {
        String objectName = String.format("transport-photos/%d/%s", id, formatter.format(LocalDateTime.now()));
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user-files")
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }
}
