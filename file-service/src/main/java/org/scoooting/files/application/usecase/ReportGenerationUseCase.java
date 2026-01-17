package org.scoooting.files.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.PdfReportGenerator;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.adapters.interfaces.kafka.dto.ReportDataDTO;
import org.scoooting.files.adapters.infrastructure.exceptions.MinioException;
import org.scoooting.files.application.services.FileFormat;

import java.io.*;

@RequiredArgsConstructor
public class ReportGenerationUseCase {

    private final StorageOperations storageOperations;
    private final PdfReportGenerator pdfReportGenerator;

    /**
     * Generate the report from template pdf file with user's credentials and its rental data.
     * @param report - all required data for pdf.
     */
    public void generateReport(ReportDataDTO report, String reportFormat, String dateFormat) {
        byte[] bytes = pdfReportGenerator.generate(report);
        String reportName = String.format(reportFormat, report.userId(),
                FileFormat.getStringDateFormat(report.endTime(), dateFormat));

        InputStream resultInputStream = new ByteArrayInputStream(bytes);
        try {
            storageOperations.upload(FileCategory.USER, reportName, resultInputStream, bytes.length,
                    "application/pdf");
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

}
