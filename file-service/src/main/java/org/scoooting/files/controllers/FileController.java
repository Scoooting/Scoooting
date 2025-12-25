package org.scoooting.files.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.files.config.UserPrincipal;
import org.scoooting.files.services.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload-transport", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadFile(@RequestPart("file") MultipartFile file) {
        fileService.uploadPhoto(1L, file);
        return ResponseEntity.ok().build();
    }
}
