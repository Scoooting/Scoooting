package org.scoooting.files.adapters.infrastructure.reports;

import lombok.RequiredArgsConstructor;
import org.openpdf.text.Element;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.PdfReportGenerator;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.adapters.interfaces.kafka.dto.ReportDataDTO;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class PdfReportGeneratorImpl implements PdfReportGenerator {

    private final StorageOperations storageOperations;

    @Override
    public byte[] generate(ReportDataDTO report) {
        try (InputStream inputStream = storageOperations.download(FileCategory.USER, "reports/Отчет об аренде.pdf")
                .inputStream();
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
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get absolute path of fonts which locate in resources directory.
     * @param font - font name
     * @return -
     */
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
