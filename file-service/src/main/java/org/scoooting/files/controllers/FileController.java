package org.scoooting.files.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.scoooting.files.config.UserPrincipal;
import org.scoooting.files.services.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${minio.user-files-bucket}")
    private String userFilesBucket;

    @Value("${minio.default-bucket}")
    private String defaultBucket;

    @PostMapping(value = "/upload-transport-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
        fileService.uploadPhoto(userPrincipal.getUserId(), userFilesBucket, file);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<InputStreamResource> uploadFile(@RequestPart("file") MultipartFile file,
                                                            @RequestParam(required = false) String bucket,
                                                            @RequestParam @NotNull String path) {
        bucket = bucket == null ? defaultBucket : bucket;
        fileService.uploadFile(bucket, file, path + "/" + file.getOriginalFilename());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<InputStreamResource> download(@RequestParam @NotBlank String path,
                                                        @RequestParam(required = false) String bucket) {
        bucket = bucket == null ? defaultBucket : bucket;
        InputStream is = fileService.getFile(bucket, path);
        String filename = fileService.getFilenameFromPath(path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=\"" + filename + "\"")
                .body(new InputStreamResource(is));
    }
}
