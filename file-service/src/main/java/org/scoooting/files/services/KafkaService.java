package org.scoooting.files.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.files.dto.ReportDataDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ReportsService reportsService;

    @KafkaListener(topics = "reports-data", groupId = "file-service")
    public void generateReport(HashMap<String, Object> message) {
        ReportDataDTO reportData = mapper.convertValue(message, ReportDataDTO.class);
        log.info("Generate report. " + reportData);
        reportsService.generateReport(reportData);
    }
}
