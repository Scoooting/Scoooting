package org.scoooting.files.services;

import lombok.RequiredArgsConstructor;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.scoooting.files.dto.ReportDataDTO;
import org.scoooting.files.exceptions.MinioException;
import org.scoooting.files.utils.FileFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final FileService fileService;
    private final FileFormat fileFormat;

    @Value("${minio.buckets.user-files}")
    private String userFilesBucket;

    public void generateReport(ReportDataDTO report) {
        try (InputStream inputStream = fileService.getObject(userFilesBucket, "reports/Отчет об аренде.pdf").inputStream();
             PdfReader pdfReader = new PdfReader(inputStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfStamper pdfStamper = new PdfStamper(pdfReader, baos)) {

            PdfContentByte canvas = pdfStamper.getOverContent(1);
            BaseFont font = BaseFont.createFont(getFontPath("DejaVuSans"), BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);

            canvas.beginText();
            canvas.setFontAndSize(font, 14);

            canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(report.rentalId()), 275, 741, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.username(), 200, 716, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.email(), 140, 691, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.transport(), 176, 665, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, getTimeString(report.startTime()), 208, 639, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, getTimeString(report.endTime()), 236, 614, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, getDurationString(report.durationMinutes()), 195, 588, 0);
            canvas.showTextAligned(Element.ALIGN_LEFT, report.status(), 148, 562, 0);

            font = BaseFont.createFont(getFontPath("DejaVuSans-Bold"), BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
            canvas.setFontAndSize(font, 14);

            canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(report.totalCost()), 148, 484, 0);
            canvas.endText();

            pdfStamper.close();
            pdfReader.close();
            writePdfToMinio(report.userId(), report.endTime(), baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePdfToMinio(Long userId, Long time, byte[] bytes) {
        String reportName = fileFormat.getFilenameWithTime(fileFormat.getReportsFormat(), userId, time);

        InputStream resultInputStream = new ByteArrayInputStream(bytes);
        try {
            fileService.uploadObject(fileService.getUserFilesBucket(), resultInputStream, reportName, bytes.length,
                    "application/pdf");
        } catch (Exception e) {
            throw new MinioException(e.getMessage());
        }
    }

    private String getFontPath(String font) {
        URL url = getClass().getClassLoader().getResource("fonts/" + font + ".ttf");
        try {
            return Paths.get(url.toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTimeString(Long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());

        String date = String.format("%02d.%02d.%d", dateTime.getDayOfMonth(), dateTime.getMonthValue(), dateTime.getYear());
        String time = String.format("%02d:%02d:%02d", dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());

        return date + " " + time;
    }

    private String getDurationString(Integer duration) {
        return String.format("%dч %dмин", duration / 60, duration % 60);
    }
}
