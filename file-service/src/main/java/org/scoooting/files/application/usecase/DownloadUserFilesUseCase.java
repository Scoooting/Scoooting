package org.scoooting.files.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.services.FileFormat;
import org.scoooting.files.application.ports.dto.LocalTimeDto;
import org.scoooting.files.application.ports.dto.FileDto;

@RequiredArgsConstructor
public class DownloadUserFilesUseCase {

    private final StorageOperations storageOperations;

    public FileDto execute(Long id, String fileFormat, String dateFormat, LocalTimeDto localTimeDto) {
        String filename = String.format(fileFormat, id,
                FileFormat.getStringDateFormat(localTimeDto, dateFormat));
        return storageOperations.download(FileCategory.USER, filename);
    }

}
