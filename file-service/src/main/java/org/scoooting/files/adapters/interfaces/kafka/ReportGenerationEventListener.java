package org.scoooting.files.adapters.interfaces.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.files.application.usecase.ReportGenerationUseCase;
import org.scoooting.files.adapters.interfaces.kafka.dto.ReportDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportGenerationEventListener {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ReportGenerationUseCase reportGenerationUseCase;

    @Value("${minio.formats.reports}")
    private String reportsFormat;

    @Value("${minio.date-format}")
    private String dateFormat;

    /**
     * This method is called when rental is ended, cancelled or force ended.
     * @param message - ReportDataDto as HashMap
     */
    @KafkaListener(topics = "reports-data", groupId = "file-service")
    public void generateReport(HashMap<String, Object> message) {
        ReportDataDTO reportData = mapper.convertValue(message, ReportDataDTO.class);
        log.info("Generate report. " + reportData);
        reportGenerationUseCase.generateReport(reportData, reportsFormat, dateFormat);
    }
}
