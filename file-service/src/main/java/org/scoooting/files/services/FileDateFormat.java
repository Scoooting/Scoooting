package org.scoooting.files.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileDateFormat {

    @Value("${minio.date-format}")
    private String dateFormat;

    public String getStringFormat(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }
}
