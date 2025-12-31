package org.scoooting.files.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileDateFormat {

    @Value("${minio.date-format}")
    private String dateFormat;

    public String getStringFormat(long seconds) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("UTC"));
        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }

    public String getStringFormat(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }
}
