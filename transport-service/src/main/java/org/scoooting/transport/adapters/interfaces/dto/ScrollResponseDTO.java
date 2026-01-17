package org.scoooting.transport.adapters.interfaces.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ScrollResponseDTO<T>(
        @NotNull List<T> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Boolean hasMore
) {}