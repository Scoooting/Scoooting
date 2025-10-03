package org.scoooting.dto;

import org.scoooting.entities.enums.ScootersStatus;

public record ScootersDTO( Long id, String model, ScootersStatus status, Float latitude, Float longitude) {}
