package org.scoooting.rental.adapters.message.feign.resilient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.adapters.message.feign.ByteArrayMultipartFile;
import org.scoooting.rental.adapters.message.feign.FeignFileClient;
import org.scoooting.rental.domain.exceptions.FileServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientFileClient {

    private final FeignFileClient fileServiceApi;

    @CircuitBreaker(name = "fileService", fallbackMethod = "uploadPhotoFallback")
    public void uploadTransportPhoto(byte[] photoBytes, Long userId) {
        log.debug("Uploading transport photo, size: {} bytes", photoBytes.length);
        try {
            MultipartFile multipartFile = new ByteArrayMultipartFile(
                    "file",
                    "transport-photo.jpg",
                    "image/jpeg",
                    photoBytes
            );

            fileServiceApi.uploadTransportPhoto(multipartFile, userId);
            log.debug("Photo uploaded successfully");
        } catch (FeignException e) {
            log.error("File service unavailable: {}", e.getMessage());
            throw new FileServiceException("File service is currently unavailable");
        }
    }

    public void uploadPhotoFallback(byte[] photoBytes, Throwable t) {
        log.error("FALLBACK uploadTransportPhoto! error: {}", t.getClass().getSimpleName());
        throw new FileServiceException("Failed to upload photo", t);
    }
}