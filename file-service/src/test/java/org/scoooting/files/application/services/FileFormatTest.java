package org.scoooting.files.application.services;

import org.junit.jupiter.api.Test;
import org.scoooting.files.application.ports.dto.LocalTimeDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileFormatTest {

    @Test
    void getStringDateFormat_FromEpochSeconds_Success() {
        // Arrange
        long epochSeconds = 1640000000L; // 2021-12-20 12:13:20 UTC
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";

        // Act
        String result = FileFormat.getStringDateFormat(epochSeconds, dateFormat);

        // Assert
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"));
    }

    @Test
    void getStringDateFormat_FromEpochSeconds_DifferentFormat() {
        // Arrange
        long epochSeconds = 1700000000L;
        String dateFormat = "dd.MM.yyyy HH:mm";

        // Act
        String result = FileFormat.getStringDateFormat(epochSeconds, dateFormat);

        // Assert
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}"));
    }

    @Test
    void getStringDateFormat_FromLocalDateTime_Success() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";

        // Act
        String result = FileFormat.getStringDateFormat(dateTime, dateFormat);

        // Assert
        assertEquals("2024-01-15_10-30-45", result);
    }

    @Test
    void getStringDateFormat_FromLocalDateTime_ShortFormat() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        String dateFormat = "yyyyMMdd";

        // Act
        String result = FileFormat.getStringDateFormat(dateTime, dateFormat);

        // Assert
        assertEquals("20231231", result);
    }

    @Test
    void getStringDateFormat_FromLocalTimeDto_Success() {
        // Arrange
        LocalTimeDto localTimeDto = new LocalTimeDto(2024, 6, 15, 14, 25, 30);
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";

        // Act
        String result = FileFormat.getStringDateFormat(localTimeDto, dateFormat);

        // Assert
        assertEquals("2024-06-15_14-25-30", result);
    }

    @Test
    void getStringDateFormat_FromLocalTimeDto_CustomFormat() {
        // Arrange
        LocalTimeDto localTimeDto = new LocalTimeDto(2024, 3, 8, 9, 5, 0);
        String dateFormat = "dd/MM/yy HH:mm";

        // Act
        String result = FileFormat.getStringDateFormat(localTimeDto, dateFormat);

        // Assert
        assertEquals("08/03/24 09:05", result);
    }

    @Test
    void getParent_NormalPath_ReturnsParent() {
        // Arrange
        String path = "users/123/reports/report.pdf";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("users/123/reports/", result);
    }

    @Test
    void getParent_NestedPath_ReturnsParent() {
        // Arrange
        String path = "a/b/c/d/file.txt";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("a/b/c/d/", result);
    }

    @Test
    void getParent_RootPath_ReturnsEmpty() {
        // Arrange
        String path = "/";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("", result);
    }

    @Test
    void getParent_DeepNestedPath_ReturnsParent() {
        // Arrange
        String path = "users/123/photos/2024/01/image.jpg";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("users/123/photos/2024/01/", result);
    }

    @Test
    void getParent_TwoLevelPath_ReturnsParent() {
        // Arrange
        String path = "folder/file.txt";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("folder/", result);
    }
}