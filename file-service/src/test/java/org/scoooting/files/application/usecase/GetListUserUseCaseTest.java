package org.scoooting.files.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetListUserUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private GetListUserUseCase getListUserUseCase;

    @Test
    void execute_Success() {
        // Arrange
        String reportsFormat = "users/%d/reports/report.pdf";
        Long userId = 123L;
        String expectedPath = "users/123/reports/";
        List<String> expectedFiles = List.of("report1.pdf", "report2.pdf");

        when(storageOperations.getListDir(FileCategory.USER, expectedPath))
                .thenReturn(expectedFiles);

        // Act
        List<String> result = getListUserUseCase.execute(reportsFormat, userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("report1.pdf", result.get(0));
        assertEquals("report2.pdf", result.get(1));
        verify(storageOperations).getListDir(FileCategory.USER, expectedPath);
    }

    @Test
    void execute_EmptyList() {
        // Arrange
        String reportsFormat = "users/%d/reports/report.pdf";
        Long userId = 456L;
        String expectedPath = "users/456/reports/";

        when(storageOperations.getListDir(FileCategory.USER, expectedPath))
                .thenReturn(List.of());

        // Act
        List<String> result = getListUserUseCase.execute(reportsFormat, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(storageOperations).getListDir(FileCategory.USER, expectedPath);
    }
}