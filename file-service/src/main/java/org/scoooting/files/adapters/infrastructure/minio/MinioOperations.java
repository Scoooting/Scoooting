package org.scoooting.files.adapters.infrastructure.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.scoooting.files.adapters.infrastructure.exceptions.MinioException;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.ports.dto.FileDto;
import org.scoooting.files.domain.exceptions.FileNotFoundException;
import org.scoooting.files.domain.exceptions.StorageTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MinioOperations implements StorageOperations {

    private final MinioClient minioClient;

    @Value("${minio.buckets.user-files}")
    private String userFilesBucket;

    @Value("${minio.buckets.default}")
    private String defaultBucket;

    @Override
    public void upload(FileCategory fileCategory, String path, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName(fileCategory))
                            .object(path)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    @Override
    public FileDto download(FileCategory fileCategory, String path) {
        try {
            String filename = Paths.get(path).getFileName().toString();
            filename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return new FileDto(filename, minioClient.getObject(GetObjectArgs.builder()
                    .bucket(getBucketName(fileCategory))
                    .object(path)
                    .build()
            ));

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey"))
                throw new FileNotFoundException(path);
            throw new StorageTechnicalException(e);

        } catch (Exception e) {
            throw new StorageTechnicalException(e);
        }
    }

    @Override
    public List<String> getListDir(FileCategory fileCategory, String path) {
        Iterable<Result<Item>> resp = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(getBucketName(fileCategory))
                .prefix(path)
                .recursive(false)
                .build());

        Iterator<Result<Item>> it = resp.iterator();
        List<String> filesList = new LinkedList<>();
        while (it.hasNext()) {
            try {
                Item i = it.next().get();
                String filename = Paths.get(i.objectName()).getFileName().toString();
                filesList.add(filename);
            } catch (ErrorResponseException e) {
                if (e.errorResponse().code().equals("NoSuchKey"))
                    throw new FileNotFoundException(path);
                throw new StorageTechnicalException(e);

            } catch (Exception e) {
                throw new StorageTechnicalException(e);
            }
        }
        return filesList;
    }

    @Override
    public void remove(FileCategory fileCategory, String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(getBucketName(fileCategory))
                    .object(path)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey"))
                throw new FileNotFoundException(path);
            throw new StorageTechnicalException(e);

        } catch (Exception e) {
            throw new StorageTechnicalException(e);
        }
    }

    private String getBucketName(FileCategory fileCategory) {
        return switch (fileCategory) {
            case USER -> userFilesBucket;
            case DEFAULT -> defaultBucket;
        };
    }
}
