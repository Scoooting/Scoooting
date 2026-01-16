package org.scoooting.files.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.files.adapters.infrastructure.exceptions.MinioException;
import org.scoooting.files.adapters.interfaces.kafka.dto.ReportDataDTO;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.PdfReportGenerator;
import org.scoooting.files.application.ports.StorageOperations;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGenerationUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @Mock
    private PdfReportGenerator pdfReportGenerator;

    @InjectMocks
    private ReportGenerationUseCase reportGenerationUseCase;

    @Test
    void generateReport_Success() {
        // Arrange
        ReportDataDTO reportData = new ReportDataDTO(
                1L,
                123L,
                "Test User",
                "test@example.com",
                "Scooter XYZ",
                1640000000L,
                1640003600L,
                60,
                "COMPLETED",
                100
        );

        String reportFormat = "users/%d/reports/%s.pdf";
        String dateFormat = "yyyy-MM-dd_HH-mm-ss";
        byte[] pdfBytes = "PDF content".getBytes();

        when(pdfReportGenerator.generate(reportData))
                .thenReturn(pdfBytes);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);

        // Act
        reportGenerationUseCase.generateReport(reportData, reportFormat, dateFormat);

        // Assert
        verify(pdfReportGenerator).generate(reportData);
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                pathCaptor.capture(),
                streamCaptor.capture(),
                eq((long) pdfBytes.length),
                eq("application/pdf")
        );

        String capturedPath = pathCaptor.getValue();
        assertTrue(capturedPath.startsWith("users/123/reports/"));
        assertTrue(capturedPath.endsWith(".pdf"));
    }

    @Test
    void generateReport_DifferentDateFormat() {
        // Arrange
        ReportDataDTO reportData = new ReportDataDTO(
                2L,
                456L,
                "Another User",
                "another@example.com",
                "Bike ABC",
                1700000000L,
                1700007200L,
                120,
                "COMPLETED",
                200
        );

        String reportFormat = "reports/%d/%s_report.pdf";
        String dateFormat = "dd.MM.yyyy";
        byte[] pdfBytes = "Another PDF".getBytes();

        when(pdfReportGenerator.generate(reportData))
                .thenReturn(pdfBytes);

        // Act
        reportGenerationUseCase.generateReport(reportData, reportFormat, dateFormat);

        // Assert
        verify(pdfReportGenerator).generate(reportData);
        verify(storageOperations).upload(
                eq(FileCategory.USER),
                anyString(),
                any(InputStream.class),
                eq((long) pdfBytes.length),
                eq("application/pdf")
        );
    }

    @Test
    void generateReport_StorageException_ThrowsMinioException() {
        // Arrange
        ReportDataDTO reportData = new ReportDataDTO(
                3L,
                789L,
                "Error User",
                "error@example.com",
                "Transport",
                1600000000L,
                1600003600L,
                60,
                "FAILED",
                50
        );

        String reportFormat = "users/%d/reports/%s.pdf";
        String dateFormat = "yyyy-MM-dd";
        byte[] pdfBytes = "PDF".getBytes();

        when(pdfReportGenerator.generate(reportData))
                .thenReturn(pdfBytes);

        doThrow(new RuntimeException("Storage error"))
                .when(storageOperations).upload(
                        any(FileCategory.class),
                        anyString(),
                        any(InputStream.class),
                        anyLong(),
                        anyString()
                );

        // Act & Assert
        assertThrows(MinioException.class,
                () -> reportGenerationUseCase.generateReport(reportData, reportFormat, dateFormat));

        verify(pdfReportGenerator).generate(reportData);
    }
}