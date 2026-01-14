package org.scoooting.files.application.services;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.scoooting.files.application.ports.dto.LocalTimeDto;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileFormat {

    // get formatted date string using seconds since 1970 year
    public static String getStringDateFormat(long seconds, String dateFormat) {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);
        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }

    // get formatted date string using seconds LocalDateTime object
    public static String getStringDateFormat(LocalDateTime localDateTime, String dateFormat) {
        return DateTimeFormatter.ofPattern(dateFormat).format(localDateTime);
    }

    // get formatted date string converting localTimeDto into localDateTime
    public static String getStringDateFormat(LocalTimeDto localTimeDto, String dateFormat) {
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

    /**
     * Get parent directory
     * @param path - path to specified directory with filename
     * @return - parent directory
     */
    public static String getParent(String path) {
        if (path.equals("/"))
            return "";
        return Paths.get(path).getParent().toString().replace("\\", "/") + "/";
    }

//    public String getFilenameWithTime(String pathFormat, long id, LocalTimeDto localTimeDto) {
//        return String.format(pathFormat, id, getStringDateFormat(localTimeDto));
//    }
//
//    public String getFilenameWithTime(String pathFormat, long id, long time) {
//        return String.format(pathFormat, id, getStringDateFormat(time));
//    }
}
