package org.scoooting.files.adapters.interfaces.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.files.application.usecase.ReportGenerationUseCase;
import org.scoooting.files.adapters.interfaces.kafka.dto.ReportDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportGenerationEventListener {

    private final ReportGenerationUseCase reportGenerationUseCase;

    @Value("${minio.formats.reports}")
    private String reportsFormat;

    @Value("${minio.date-format}")
    private String dateFormat;

    /**
     * This method is called when rental is ended, cancelled or force ended.
     */
    @KafkaListener(topics = "reports-data", groupId = "file-service")
    public void generateReport(
            @Payload @Valid ReportDataDTO reportData,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            log.info("Received report generation request from topic={}, partition={}, offset={}: {}",
                    topic, partition, offset, reportData);
            reportGenerationUseCase.generateReport(reportData, reportsFormat, dateFormat);
            log.info("Report generated successfully for rental {}", reportData.rentalId());
        } catch (Exception e) {
            log.error("Failed to generate report for rental {}: {}", reportData.rentalId(), e.getMessage(), e);
        }
    }}
