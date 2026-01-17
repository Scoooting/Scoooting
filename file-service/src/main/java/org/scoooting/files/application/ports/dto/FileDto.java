package org.scoooting.files.application.ports.dto;

import java.io.InputStream;

public record FileDto(String filename, InputStream inputStream) {}
