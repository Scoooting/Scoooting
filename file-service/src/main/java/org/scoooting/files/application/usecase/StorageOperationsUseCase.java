package org.scoooting.files.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.ports.dto.FileDto;

import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
public class StorageOperationsUseCase {

    private final StorageOperations storageOperations;

    public void upload(FileCategory fileCategory, String path, InputStream inputStream, long size, String contentType) {
        storageOperations.upload(fileCategory, path, inputStream, size, contentType);
    }

    public FileDto download(FileCategory fileCategory, String path) {
        return storageOperations.download(fileCategory, path);
    }

    public List<String> getListDir(FileCategory fileCategory, String path) {
        return storageOperations.getListDir(fileCategory, path);
    }

    public void remove(FileCategory fileCategory, String path) {
        storageOperations.remove(fileCategory, path);
    }
}
