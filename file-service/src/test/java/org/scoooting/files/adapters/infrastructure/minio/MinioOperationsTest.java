package org.scoooting.files.adapters.infrastructure.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.dto.FileDto;
import org.scoooting.files.domain.exceptions.FileNotFoundException;
import org.scoooting.files.domain.exceptions.StorageTechnicalException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioOperationsTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ObjectWriteResponse objectWriteResponse;

    @Mock
    private GetObjectResponse getObjectResponse;

    private MinioOperations minioOperations;

    @BeforeEach
    void setUp() {
        minioOperations = new MinioOperations(minioClient);
        ReflectionTestUtils.setField(minioOperations, "userFilesBucket", "user-files");
        ReflectionTestUtils.setField(minioOperations, "defaultBucket", "default");
    }

    @Test
    void upload_Success() throws Exception {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "test/file.txt";
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        long size = 100L;
        String contentType = "text/plain";

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(objectWriteResponse);

        // Act & Assert
        assertDoesNotThrow(() ->
                minioOperations.upload(category, path, inputStream, size, contentType)
        );

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_DefaultBucket_Success() throws Exception {
        // Arrange
        FileCategory category = FileCategory.DEFAULT;
        String path = "default/file.pdf";
        InputStream inputStream = new ByteArrayInputStream("pdf".getBytes());
        long size = 50L;
        String contentType = "application/pdf";

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(objectWriteResponse);

        // Act
        minioOperations.upload(category, path, inputStream, size, contentType);

        // Assert
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_ThrowsMinioException_OnError() throws Exception {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "error/file.txt";
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("Upload failed"));

        // Act & Assert
        assertThrows(Exception.class,
                () -> minioOperations.upload(category, path, inputStream, 50L, "text/plain"));
    }

    @Test
    void download_Success() throws Exception {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "downloads/file.pdf";

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(getObjectResponse);

        // Act
        FileDto result = minioOperations.download(category, path);

        // Assert
        assertNotNull(result);
        assertEquals("file.pdf", result.filename());
        assertNotNull(result.inputStream());
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void download_FileNotFound_ThrowsFileNotFoundException() throws Exception {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "missing/file.txt";

        ErrorResponse errorResponse = new ErrorResponse(
                "NoSuchKey",
                "The specified key does not exist",
                "user-files",
                path,
                "",
                "",
                ""
        );
        ErrorResponseException exception = new ErrorResponseException(
                errorResponse,
                null,
                "test"
        );

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(exception);

        // Act & Assert
        FileNotFoundException thrown = assertThrows(FileNotFoundException.class,
                () -> minioOperations.download(category, path));

        assertTrue(thrown.getMessage().contains(path));
    }

    @Test
    void download_TechnicalError_ThrowsStorageTechnicalException() throws Exception {
        // Arrange
        FileCategory category = FileCategory.DEFAULT;
        String path = "error/file.txt";

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(StorageTechnicalException.class,
                () -> minioOperations.download(category, path));
    }

    @Test
    void getListDir_EmptyDirectory_ReturnsEmptyList() {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "empty/";

        when(minioClient.listObjects(any(ListObjectsArgs.class)))
                .thenReturn(new ArrayList<>());

        // Act
        var result = minioOperations.getListDir(category, path);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void remove_Success() throws Exception {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "temp/file.jpg";

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        assertDoesNotThrow(() -> minioOperations.remove(category, path));
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void remove_FileNotFound_ThrowsFileNotFoundException() throws Exception {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "missing/file.txt";

        ErrorResponse errorResponse = new ErrorResponse(
                "NoSuchKey",
                "The specified key does not exist",
                "user-files",
                path,
                "",
                "",
                ""
        );
        ErrorResponseException exception = new ErrorResponseException(
                errorResponse,
                null,
                "test"
        );

        doThrow(exception).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        assertThrows(FileNotFoundException.class,
                () -> minioOperations.remove(category, path));
    }

    @Test
    void remove_TechnicalError_ThrowsStorageTechnicalException() throws Exception {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "error/file.txt";

        doThrow(new RuntimeException("Connection error"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        assertThrows(StorageTechnicalException.class,
                () -> minioOperations.remove(category, path));
    }
}