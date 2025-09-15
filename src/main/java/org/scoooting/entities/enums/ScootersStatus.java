package org.scoooting.entities.enums;

public enum ScootersStatus {
    FREE,         // Available for rental
    BUSY,         // Currently rented
    NONACTIVE,    // Deactivated/offline
    MAINTENANCE,  // Being serviced
    LOW_BATTERY,  // Needs charging
    DAMAGED       // Needs repair
}
