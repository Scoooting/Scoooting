package org.scoooting.files.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.ports.dto.FileDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageOperationsUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private StorageOperationsUseCase storageOperationsUseCase;

    @Test
    void upload_Success() {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "test/file.txt";
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        long size = 100L;
        String contentType = "text/plain";

        // Act
        storageOperationsUseCase.upload(category, path, inputStream, size, contentType);

        // Assert
        verify(storageOperations).upload(category, path, inputStream, size, contentType);
    }

    @Test
    void download_Success() {
        // Arrange
        FileCategory category = FileCategory.DEFAULT;
        String path = "downloads/file.pdf";
        InputStream mockStream = new ByteArrayInputStream("pdf data".getBytes());
        FileDto expectedFileDto = new FileDto("file.pdf", mockStream);

        when(storageOperations.download(category, path))
                .thenReturn(expectedFileDto);

        // Act
        FileDto result = storageOperationsUseCase.download(category, path);

        // Assert
        assertNotNull(result);
        assertEquals("file.pdf", result.filename());
        verify(storageOperations).download(category, path);
    }

    @Test
    void getListDir_Success() {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "user/123/";
        List<String> expectedFiles = List.of("file1.txt", "file2.pdf");

        when(storageOperations.getListDir(category, path))
                .thenReturn(expectedFiles);

        // Act
        List<String> result = storageOperationsUseCase.getListDir(category, path);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("file1.txt", result.get(0));
        verify(storageOperations).getListDir(category, path);
    }

    @Test
    void remove_Success() {
        // Arrange
        FileCategory category = FileCategory.USER;
        String path = "temp/old-file.jpg";

        // Act
        storageOperationsUseCase.remove(category, path);

        // Assert
        verify(storageOperations).remove(category, path);
    }

    @Test
    void getListDir_EmptyDirectory() {
        // Arrange
        FileCategory category = FileCategory.DEFAULT;
        String path = "empty/";

        when(storageOperations.getListDir(category, path))
                .thenReturn(List.of());

        // Act
        List<String> result = storageOperationsUseCase.getListDir(category, path);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(storageOperations).getListDir(category, path);
    }
}