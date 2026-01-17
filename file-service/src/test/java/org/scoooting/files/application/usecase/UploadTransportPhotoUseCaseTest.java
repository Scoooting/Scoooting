package org.scoooting.files.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadTransportPhotoUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private UploadTransportPhotoUseCase uploadTransportPhotoUseCase;

    @Test
    void execute_Success() {
        // Arrange
        Long userId = 123L;
        String transportPhotosFormat = "users/%d/photos/%s.jpg";
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";
        InputStream inputStream = new ByteArrayInputStream("image data".getBytes());
        long size = 1024L;

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        uploadTransportPhotoUseCase.execute(userId, transportPhotosFormat, dateFormat, inputStream, size);

        // Assert
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                pathCaptor.capture(),
                eq(inputStream),
                eq(size),
                eq("image/jpeg")
        );

        String capturedPath = pathCaptor.getValue();
        assertTrue(capturedPath.startsWith("users/123/photos/"));
        assertTrue(capturedPath.endsWith(".jpg"));
    }

    @Test
    void execute_DifferentUser() {
        // Arrange
        Long userId = 999L;
        String transportPhotosFormat = "transport/%d/images/%s.jpeg";
        String dateFormat = "yyyyMMdd_HHmmss";
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        long size = 2048L;

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        uploadTransportPhotoUseCase.execute(userId, transportPhotosFormat, dateFormat, inputStream, size);

        // Assert
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                pathCaptor.capture(),
                eq(inputStream),
                eq(size),
                eq("image/jpeg")
        );

        String capturedPath = pathCaptor.getValue();
        assertTrue(capturedPath.startsWith("transport/999/images/"));
        assertTrue(capturedPath.endsWith(".jpeg"));
    }
}