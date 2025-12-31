package org.scoooting.files.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.scoooting.files.dto.request.LocalTimeDto;
import org.scoooting.files.exceptions.FileTypeException;
import org.scoooting.files.exceptions.MinioConnectionException;
import org.scoooting.files.utils.FileDateFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@Getter
@RequiredArgsConstructor
public class FileService {

    @Value("${minio.paths.transport-photos}")
    private String transportPhotosPath;

    @Value("${minio.buckets.user-files}")
    private String userFilesBucket;

    private final MinioClient minioClient;
    private final FileDateFormat fileDateFormat;

    public void uploadFile(String bucketName, InputStream is, String path, long size, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(is, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }
    public void uploadFile(String bucketName, MultipartFile file, String path) {
        try (InputStream is = file.getInputStream()) {
            uploadFile(bucketName, is, path, file.getSize(), file.getContentType());
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
