package org.scoooting.transport.dto.response;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ScrollResponseDTO<T>(
        @NotNull List<T> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Boolean hasMore
) {}