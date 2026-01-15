package org.scoooting.files.application.ports;

import org.scoooting.files.application.model.FileCategory;
import org.scoooting.files.application.ports.dto.FileDto;

import java.io.InputStream;
import java.util.List;

/**
 * basic methods to interact with storage
 */
public interface StorageOperations {

    /**
     * Upload file to storage. Main method
     * @param fileCategory - category of file
     * @param inputStream - object of InputStream interface
     * @param path - path to specified directory with filename
     * @param size - size of file
     * @param contentType - content type
     */

    void upload(FileCategory fileCategory, String path, InputStream inputStream, long size, String contentType);

    /**
     * Dowload file from storage
     * @param fileCategory - category of file
     * @param path - path to specified directory with filename
     * @return - FileDto. Include:
     *                    - filename
     *                    - Object of Input Stream
     */
    FileDto download(FileCategory fileCategory, String path);

    /**
     * Get file list of any specified path
     * @param fileCategory - category of file
     * @param path - path to specified directory
     * @return - list of file names
     */
    List<String> getListDir(FileCategory fileCategory, String path);

    /**
     * Remove file
     * @param fileCategory - category of file
     * @param path - path to specified directory
     */
    void remove(FileCategory fileCategory, String path);
}
