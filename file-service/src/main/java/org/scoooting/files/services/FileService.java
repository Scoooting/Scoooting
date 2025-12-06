package org.scoooting.files.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${photos.transport-path}")
    private String photosPath;

    private void checkDirectory(Path path) throws IOException {
        if (!Files.exists(path))
            Files.createDirectories(path);
    }

    public Mono<Void> uploadPhoto(Long id, FilePart file) throws IOException {
        String fullPath = photosPath + "/" + id;
        checkDirectory(Path.of(fullPath));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return file.transferTo(Path.of(fullPath + "/" + formatter.format(LocalDateTime.now())));
    }
}
