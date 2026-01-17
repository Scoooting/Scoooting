package org.scoooting.files.adapters.infrastructure.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.files.adapters.interfaces.kafka.dto.ReportDataDTO;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.ports.dto.FileDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfReportGeneratorImplTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private PdfReportGeneratorImpl pdfReportGenerator;

    private ReportDataDTO reportData;

    @BeforeEach
    void setUp() {
        reportData = new ReportDataDTO(
                1L,
                123L,
                "Иван Иванов",
                "ivan@example.com",
                "Самокат XYZ-100",
                1640000000L,
                1640003600L,
                60,
                "ЗАВЕРШЕНО",
                250
        );
    }

    @Test
    void generate_Success_ReturnsPdfBytes() throws Exception {
        // Arrange
        byte[] templatePdf = createMinimalPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdf);
        FileDto templateFileDto = new FileDto("template.pdf", templateStream);

        when(storageOperations.download(
                eq(FileCategory.USER),
                eq("reports/Отчет об аренде.pdf")
        )).thenReturn(templateFileDto);

        // Act
        byte[] result = pdfReportGenerator.generate(reportData);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(storageOperations).download(FileCategory.USER, "reports/Отчет об аренде.pdf");
    }

    @Test
    void generate_DifferentData_ReturnsPdfBytes() throws Exception {
        // Arrange
        ReportDataDTO differentData = new ReportDataDTO(
                2L,
                456L,
                "Петр Петров",
                "petr@test.com",
                "Велосипед ABC-200",
                1700000000L,
                1700007200L,
                120,
                "ОТМЕНЕНО",
                500
        );

        byte[] templatePdf = createMinimalPdfTemplate();
        InputStream templateStream = new ByteArrayInputStream(templatePdf);
        FileDto templateFileDto = new FileDto("template.pdf", templateStream);

        when(storageOperations.download(
                eq(FileCategory.USER),
                eq("reports/Отчет об аренде.pdf")
        )).thenReturn(templateFileDto);

        // Act
        byte[] result = pdfReportGenerator.generate(differentData);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generate_ThrowsRuntimeException_WhenTemplateNotFound() {
        // Arrange
        when(storageOperations.download(any(), any()))
                .thenThrow(new RuntimeException("Template not found"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> pdfReportGenerator.generate(reportData));
    }

    /**
     * Создаёт минимальный валидный PDF для тестирования
     */
    private byte[] createMinimalPdfTemplate() {
        // Минимальный валидный PDF документ (пустой)
        String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<< /Length 44 >>\n" +
                "stream\n" +
                "BT\n" +
                "/F1 12 Tf\n" +
                "100 700 Td\n" +
                "(Test) Tj\n" +
                "ET\n" +
                "endstream\n" +
                "endobj\n" +
                "xref\n" +
                "0 5\n" +
                "0000000000 65535 f \n" +
                "0000000009 00000 n \n" +
                "0000000058 00000 n \n" +
                "0000000115 00000 n \n" +
                "0000000229 00000 n \n" +
                "trailer\n" +
                "<< /Size 5 /Root 1 0 R >>\n" +
                "startxref\n" +
                "322\n" +
                "%%EOF";

        return pdfContent.getBytes();
    }
}