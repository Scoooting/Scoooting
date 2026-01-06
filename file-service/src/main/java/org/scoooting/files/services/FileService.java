package org.scoooting.files.services;

import io.minio.*;
import io.minio.messages.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.scoooting.files.dto.request.LocalTimeDto;
import org.scoooting.files.dto.response.FileDto;
import org.scoooting.files.exceptions.FileTypeException;
import org.scoooting.files.exceptions.MinioException;
import org.scoooting.files.utils.FileFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Service
@Getter
@RequiredArgsConstructor
public class FileService {

    @Value("${minio.buckets.user-files}")
    private String userFilesBucket;

    private final MinioClient minioClient;
    private final FileFormat fileFormat;

    /**
     * Get file list of any specified path
     * @param bucket - bucket name
     * @param path - path to specified directory
     * @return - list of file names
     */
    public List<String> getListDir(String bucket, String path) {
        Iterable<Result<Item>> resp = minioClient.listObjects(ListObjectsArgs.builder()
               .bucket(bucket)
               .prefix(path)
               .recursive(false)
               .build());

        Iterator<Result<Item>> it = resp.iterator();
        List<String> filesList = new LinkedList<>();
        while (it.hasNext()) {
           try {
               Item i = it.next().get();
               filesList.add(getFilenameFromPath(i.objectName()));
           } catch (Exception e) {
               throw new MinioException(e.getMessage());
           }
       }
       return filesList;
    }

    /**
     * Upload file to storage. Main method
     * @param bucketName - bucket name
     * @param is - object of InputStream interface
     * @param path - path to specified directory with filename
     * @param size - size of file
     * @param contentType - content type
     */
    public void uploadObject(String bucketName, InputStream is, String path, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(is, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    /**
     * Upload file to storage.
     * @param bucketName - bucket name
     * @param file - Spring file object
     * @param path - path to specified directory with filename
     */
    public void uploadObject(String bucketName, MultipartFile file, String path) {
        try (InputStream is = file.getInputStream()) {
            uploadObject(bucketName, is, path, file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Upload photo to storage
     * @param id - user ID
     * @param bucketName - bucket name
     * @param file - Spring object of image. JPEG only!
     */
    public void uploadPhoto(Long id, String bucketName, MultipartFile file) {
        if (!file.getContentType().equals("image/jpeg"))
            throw new FileTypeException("image/jpeg", file.getContentType());

        String objectName = String.format(fileFormat.getTransportPhotosFormat(), id,
                fileFormat.getStringDateFormat(LocalDateTime.now()));

        uploadObject(bucketName, file, objectName);
    }

    /**
     * Get file from storage
     * @param bucketName - bucket name
     * @param object - path to specified directory with filename
     * @return - FileDto. Include:
     *                    - filename
     *                    - Object of Input Stream
     */
    public FileDto getObject(String bucketName, String object) {
        try {
            return new FileDto(object, minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(object)
                    .build()
            ));

        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    /**
     * Remove file
     * @param bucketName - bucket name
     * @param object - path to specified directory with filename
     */
    public void removeObject(String bucketName, String object) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(object)
                    .build());
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    /**
     * Get file with timestamp suffix. Uses for photos and reports.
     * @param pathFormat - format of file
     * @param userId - user ID
     * @param localTimeDto - timestamp
     * @return - FileDto. Include:
     *                    - filename
     *                    - Object of Input Stream
     */
    public FileDto getFileWithTimestamp(String pathFormat, Long userId, LocalTimeDto localTimeDto) {
        String filename = fileFormat.getFilenameWithTime(pathFormat, userId, localTimeDto);
        return getObject(userFilesBucket, filename);
    }

    /**
     * Extract filename from full path
     * @param path - path to specified directory with filename
     * @return filename
     */
    public String getFilenameFromPath(String path) {
        return Paths.get(path).getFileName().toString();
    }

    /**
     * Get parent directory
     * @param path - path to specified directory with filename
     * @return - parent directory
     */
    public String getParent(String path) {
        if (path.equals("/"))
            return "";
        return Paths.get(path).getParent().toString().replace("\\", "/") + "/";
    }

}
