package org.scoooting.files.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.scoooting.files.dto.request.LocalTimeDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Getter
@RequiredArgsConstructor
public class FileFormat {

    @Value("${minio.date-format}")
    private String dateFormat;

    @Value("${minio.formats.transport-photos}")
    private String transportPhotosFormat;

    @Value("${minio.formats.reports}")
    private String reportsFormat;

    public String getStringDateFormat(long seconds) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }

    public String getStringDateFormat(LocalDateTime localDateTime) {
        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }

    public String getStringDateFormat(LocalTimeDto localTimeDto) {
        LocalDateTime localDateTime = LocalDateTime.of(
                localTimeDto.year(),
                localTimeDto.month(),
                localTimeDto.day(),
                localTimeDto.hour(),
                localTimeDto.minute(),
                localTimeDto.second()
        );

        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }

    public String getFilenameWithTime(String pathFormat, long id, LocalTimeDto localTimeDto) {
        return String.format(pathFormat, id, getStringDateFormat(localTimeDto));
    }

    public String getFilenameWithTime(String pathFormat, long id, long time) {
        return String.format(pathFormat, id, getStringDateFormat(time));
    }
}
