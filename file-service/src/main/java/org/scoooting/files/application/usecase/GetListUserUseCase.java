package org.scoooting.files.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.StorageOperations;
import org.scoooting.files.application.services.FileFormat;

import java.util.List;

@RequiredArgsConstructor
public class GetListUserUseCase {

    private final StorageOperations storageOperations;

    public List<String> execute(String reportsFormat, Long id) {
        String path = String.format(FileFormat.getParent(reportsFormat), id);
        return storageOperations.getListDir(FileCategory.USER, path);
    }

}
