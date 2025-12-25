package org.scoooting.files.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.scoooting.files.dto.request.LocalTimeDto;
import org.scoooting.files.exceptions.FileTypeException;
import org.scoooting.files.exceptions.MinioConnectionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${minio.transport-photos}")
    private String transportPhotosPath;

    private final MinioClient minioClient;
    private final FileDateFormat fileDateFormat;

    public void uploadFile(String bucketName, MultipartFile file, String path) {
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new MinioConnectionException("Minio is unavailable");
        }
    }

    public void uploadPhoto(Long id, String bucketName, MultipartFile file) {
        if (!file.getContentType().equals("image/jpeg"))
            throw new FileTypeException("image/jpeg", file.getContentType());

        String objectName = String.format("%s/%d/%s.jpeg", transportPhotosPath, id,
                fileDateFormat.getStringFormat(LocalDateTime.now()));

        uploadFile(bucketName, file, objectName);
    }

    public InputStream getFile(String bucketName, String filename) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getPhoto(String bucketName, Long id, LocalTimeDto localTimeDto) {
        LocalDateTime localDateTime = LocalDateTime.of(
                localTimeDto.year(),
                localTimeDto.month(),
                localTimeDto.day(),
                localTimeDto.hour(),
                localTimeDto.minute(),
                localTimeDto.second()
        );

        String filename = String.format("%s/%d/%s", transportPhotosPath, id, fileDateFormat.getStringFormat(localDateTime));
        return getFile(bucketName, filename);
    }

    public String getFilenameFromPath(String path) {
        return Paths.get(path).getFileName().toString();
    }
}
