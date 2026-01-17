package org.scoooting.files.adapters.infrastructure.config;

import org.scoooting.files.adapters.infrastructure.minio.MinioOperations;
import org.scoooting.files.adapters.infrastructure.reports.PdfReportGeneratorImpl;
import org.scoooting.files.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public DownloadUserFilesUseCase downloadUserFilesUseCase(MinioOperations storageOperations) {
        return new DownloadUserFilesUseCase(storageOperations);
    }

    @Bean
    public GetListUserUseCase getListUserUseCase(MinioOperations storageOperations) {
        return new GetListUserUseCase(storageOperations);
    }

    @Bean
    public ReportGenerationUseCase reportGenerationUseCase(MinioOperations storageOperations,
                                                           PdfReportGeneratorImpl reportGenerator) {
        return new ReportGenerationUseCase(storageOperations, reportGenerator);
    }

    @Bean
    public StorageOperationsUseCase storageOperationsUseCase(MinioOperations storageOperations) {
        return new StorageOperationsUseCase(storageOperations);
    }

    @Bean
    public UploadTransportPhotoUseCase uploadTransportPhotoUseCase(MinioOperations storageOperations) {
        return new UploadTransportPhotoUseCase(storageOperations);
    }
}
