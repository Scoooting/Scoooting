package org.scoooting.files.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.ports.dto.FileDto;
import org.scoooting.files.application.ports.dto.LocalTimeDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadUserFilesUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private DownloadUserFilesUseCase downloadUserFilesUseCase;

    @Test
    void execute_Success() {
        // Arrange
        Long userId = 123L;
        String fileFormat = "users/%d/reports/%s.pdf";
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";
        LocalTimeDto localTimeDto = new LocalTimeDto(2024, 1, 15, 10, 30, 45);

        String expectedFilename = "users/123/reports/2024-01-15_10-30-45.pdf";
        InputStream mockStream = new ByteArrayInputStream("test data".getBytes());
        FileDto expectedFileDto = new FileDto("report.pdf", mockStream);

        when(storageOperations.download(FileCategory.USER, expectedFilename))
                .thenReturn(expectedFileDto);

        // Act
        FileDto result = downloadUserFilesUseCase.execute(userId, fileFormat, dateFormat, localTimeDto);

        // Assert
        assertNotNull(result);
        assertEquals("report.pdf", result.filename());
        assertNotNull(result.inputStream());
        verify(storageOperations).download(FileCategory.USER, expectedFilename);
    }

    @Test
    void execute_DifferentDateFormat() {
        // Arrange
        Long userId = 456L;
        String fileFormat = "users/%d/docs/%s.pdf";
        String dateFormat = "dd.MM.yyyy";
        LocalTimeDto localTimeDto = new LocalTimeDto(2024, 12, 31, 23, 59, 59);

        String expectedFilename = "users/456/docs/31.12.2024.pdf";
        InputStream mockStream = new ByteArrayInputStream("test".getBytes());
        FileDto expectedFileDto = new FileDto("doc.pdf", mockStream);

        when(storageOperations.download(FileCategory.USER, expectedFilename))
                .thenReturn(expectedFileDto);

        // Act
        FileDto result = downloadUserFilesUseCase.execute(userId, fileFormat, dateFormat, localTimeDto);

        // Assert
        assertNotNull(result);
        verify(storageOperations).download(FileCategory.USER, expectedFilename);
    }
}