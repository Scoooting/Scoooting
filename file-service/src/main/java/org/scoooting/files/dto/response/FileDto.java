package org.scoooting.files.dto.response;

import java.io.InputStream;

public record FileDto(String filename, InputStream inputStream) {}
