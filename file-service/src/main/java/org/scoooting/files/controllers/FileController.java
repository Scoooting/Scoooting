package org.scoooting.files.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.scoooting.files.config.UserPrincipal;
import org.scoooting.files.dto.request.LocalTimeDto;
import org.scoooting.files.dto.response.FileDto;
import org.scoooting.files.services.FileService;
import org.scoooting.files.utils.FileFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileFormat fileFormat;

    @Value("${minio.buckets.user-files}")
    private String userFilesBucket;

    @Value("${minio.buckets.default}")
    private String defaultBucket;

    @GetMapping("/get-transport-photos-list")
    public ResponseEntity<List<String>> getTransportPhotosList(@AuthenticationPrincipal UserPrincipal principal) {
        String path = String.format(fileService.getParent(fileFormat.getTransportPhotosFormat()), principal.getUserId());
        List<String> files = fileService.getListDir(userFilesBucket, path);
        return ResponseEntity.ok(files);
    }

    @PostMapping(value = "/upload-transport-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
        fileService.uploadPhoto(userPrincipal.getUserId(), userFilesBucket, file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/download-report")
    public ResponseEntity<InputStreamResource> downloadReport(
            @RequestBody LocalTimeDto localTimeDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FileDto fileDto = fileService.getFileWithTimestamp(
                fileFormat.getReportsFormat(), principal.getUserId(), localTimeDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    @PostMapping("/download-photo")
    public ResponseEntity<InputStreamResource> downloadPhoto(
            @RequestBody LocalTimeDto localTimeDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FileDto fileDto = fileService.getFileWithTimestamp(
                fileFormat.getTransportPhotosFormat(), principal.getUserId(), localTimeDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    @GetMapping("/get-files-list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<String>> getFilesList(@RequestParam(defaultValue = "") String path,
                                                     @RequestParam(required = false) String bucket) {
        bucket = bucket == null ? defaultBucket : bucket;
        List<String> files = fileService.getListDir(bucket, path);
        return ResponseEntity.ok(files);
    }

    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<InputStreamResource> uploadFile(@RequestPart("file") MultipartFile file,
                                                            @RequestParam(required = false) String bucket,
                                                            @RequestParam @NotNull String path) {
        bucket = bucket == null ? defaultBucket : bucket;
        fileService.uploadObject(bucket, file, path + "/" + file.getOriginalFilename());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download-file")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam @NotBlank String path,
                                                        @RequestParam(required = false) String bucket) {
        bucket = bucket == null ? defaultBucket : bucket;
        FileDto fileDto = fileService.getObject(bucket, path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    @DeleteMapping("/remove-file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeFile(@RequestParam @NotBlank String path,
                                           @RequestParam(required = false) String bucket) {
        bucket = bucket == null ? defaultBucket : bucket;
        fileService.removeObject(bucket, path);
        return ResponseEntity.ok().build();
    }
}
