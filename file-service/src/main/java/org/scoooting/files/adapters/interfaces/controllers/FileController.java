package org.scoooting.files.adapters.interfaces.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.scoooting.files.adapters.infrastructure.security.UserPrincipal;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.usecase.DownloadUserFilesUseCase;
import org.scoooting.files.application.usecase.GetListUserUseCase;
import org.scoooting.files.application.usecase.StorageOperationsUseCase;
import org.scoooting.files.application.usecase.UploadTransportPhotoUseCase;
import org.scoooting.files.application.ports.dto.LocalTimeDto;
import org.scoooting.files.application.ports.dto.FileDto;
import org.scoooting.files.domain.exceptions.FileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadTransportPhotoUseCase uploadTransportPhotoUseCase;
    private final DownloadUserFilesUseCase downloadUserFilesUseCase;
    private final GetListUserUseCase getListUserUseCase;
    private final StorageOperationsUseCase storageOperationsUseCase;

    @Value("${minio.formats.transport-photos}")
    private String transportPhotosFormat;

    @Value("${minio.formats.reports}")
    private String reportsFormat;

    @Value("${minio.date-format}")
    private String dateFormat;

    // ==================== USER OPERATIONS ====================

    @Operation(
            summary = "[USER] Get user's list of reports",
            tags = {"User File Operations"}
    )
    @GetMapping("/get-reports-list")
    public ResponseEntity<List<String>> getReportsList(@AuthenticationPrincipal UserPrincipal principal) {
        List<String> files = getListUserUseCase.execute(reportsFormat, principal.getUserId());
        return ResponseEntity.ok(files);
    }

    @Operation(
            summary = "[USER] Upload photo of transport",
            tags = {"User File Operations"}
    )
    @PostMapping(value = "/upload-transport-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) throws IOException {

        if (!file.getContentType().equals("image/jpeg"))
            throw new FileTypeException("image/jpeg", file.getContentType());
        uploadTransportPhotoUseCase.execute(userPrincipal.getUserId(), transportPhotosFormat, dateFormat,
                file.getInputStream(), file.getSize());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "[USER] Download report for the specified date",
            tags = {"User File Operations"}
    )
    @PostMapping("/download-report")
    public ResponseEntity<InputStreamResource> downloadReport(
            @RequestBody LocalTimeDto localTimeDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FileDto fileDto = downloadUserFilesUseCase.execute(principal.getUserId(), reportsFormat,
                dateFormat, localTimeDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    @Operation(
            summary = "[USER] Download photo for the specified date",
            tags = {"User File Operations"}
    )
    @PostMapping("/download-photo")
    public ResponseEntity<InputStreamResource> downloadPhoto(
            @RequestBody LocalTimeDto localTimeDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FileDto fileDto = downloadUserFilesUseCase.execute(principal.getUserId(),
                transportPhotosFormat, dateFormat, localTimeDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-disposition", "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    // ==================== SUPPORT OPERATIONS ====================

    @Operation(
            summary = "[SUPPORT] Get file list of any specified path",
            tags = {"Support File Operations"}
    )
    @GetMapping("/get-files-list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<String>> getFilesList(@RequestParam(defaultValue = "") String path,
                                                     @RequestParam(required = false) FileCategory category) {
        category = category == null ? FileCategory.DEFAULT : category;
        List<String> files = storageOperationsUseCase.getListDir(category, path);
        return ResponseEntity.ok(files);
    }

    @Operation(
            summary = "[SUPPORT] Download file of any specified path",
            tags = {"Support File Operations"}
    )
    @GetMapping("/download-file")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam @NotBlank String path,
                                                        @RequestParam(required = false) FileCategory category) {
        category = category == null ? FileCategory.DEFAULT : category;
        FileDto fileDto = storageOperationsUseCase.download(category, path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.filename() + "\"")
                .body(new InputStreamResource(fileDto.inputStream()));
    }

    // ==================== ADMIN ONLY OPERATIONS ====================

    @Operation(
            summary = "[ADMIN] Upload file to storage",
            tags = {"Admin File Operations"}
    )
    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> uploadFile(@RequestPart("file") MultipartFile file,
                                                          @RequestParam(required = false) FileCategory category,
                                                          @RequestParam @NotNull String path) throws IOException {
        category = category == null ? FileCategory.DEFAULT : category;
        storageOperationsUseCase.upload(category,path + "/" + file.getOriginalFilename(), file.getInputStream(),
                file.getSize(), file.getContentType());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "[ADMIN] Remove any file",
            tags = {"Admin File Operations"}
    )
    @DeleteMapping("/remove-file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeFile(@RequestParam @NotBlank String path,
                                           @RequestParam(required = false) FileCategory category) {
        category = category == null ? FileCategory.DEFAULT : category;
        storageOperationsUseCase.remove(category, path);
        return ResponseEntity.ok().build();
    }
}
