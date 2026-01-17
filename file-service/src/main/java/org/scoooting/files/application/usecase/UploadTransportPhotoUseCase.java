package org.scoooting.files.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.services.FileFormat;

import java.io.InputStream;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class UploadTransportPhotoUseCase {

    private final StorageOperations storageOperations;
    /**
     * Upload photo to storage
     * @param id - user ID
     * @param transportPhotosFormat - format of filename of user's transport photos
     * @param inputStream - file as InputStream object
     */
    public void execute(Long id, String transportPhotosFormat, String dateFormat, InputStream inputStream, long size) {
        String objectName = String.format(transportPhotosFormat, id,
                FileFormat.getStringDateFormat(LocalDateTime.now(), dateFormat));

        storageOperations.upload(FileCategory.USER, objectName, inputStream, size, "image/jpeg");
    }
}
