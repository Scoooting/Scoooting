package org.scoooting.files.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.files.config.UserPrincipal;
import org.scoooting.files.services.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload-transport", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Void>> uploadFile(
            @RequestPart("file") FilePart file,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        return fileService.uploadPhoto(principal.getUserId(), file).thenReturn(ResponseEntity.ok().build());
    }
}
