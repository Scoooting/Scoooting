package org.scoooting.utils;

import org.scoooting.entities.Transport;
import org.scoooting.entities.enums.TransportType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock API for generating initial transport data (replaces ScooterAPI)
 */
@Deprecated
public class TransportAPI {

    protected static final double SPB_LATITUDE_MIN = 59.823535;
    protected static final double SPB_LATITUDE_MAX = 60.041664;
    protected static final double SPB_LONGITUDE_MIN = 30.184844;
    protected static final double SPB_LONGITUDE_MAX = 30.431322;

    /**
     * Generate diverse transport fleet for testing
     */
    public List<Transport> generateInitialTransports() {
        List<Transport> transports = new ArrayList<>();

        // Generate different types of transport
        for (long i = 1; i <= 100; i++) {
            float latitude = (float) (Math.random() * (SPB_LATITUDE_MAX - SPB_LATITUDE_MIN) + SPB_LATITUDE_MIN);
            float longitude = (float) (Math.random() * (SPB_LONGITUDE_MAX - SPB_LONGITUDE_MIN) + SPB_LONGITUDE_MAX);

            Transport transport = createRandomTransport(i, latitude, longitude);
            transport.setId(null);
            transports.add(transport);
        }

        return transports;
    }

    private Transport createRandomTransport(long id, float latitude, float longitude) {
        TransportType[] types = TransportType.values();
        TransportType type = types[(int) (Math.random() * types.length)];

        String model = generateModelName(type, id);
        BigDecimal battery = isElectricType(type) ?
                BigDecimal.valueOf(50 + Math.random() * 50) : // 50-100% battery
                null;

        Transport transport = new Transport();
        transport.setId(id);
        transport.setModel(model);
        transport.setType(type);
        transport.setStatus(TransportStatus.AVAILABLE);
        transport.setLatitude(latitude);
        transport.setLongitude(longitude);
        transport.setBatteryLevel(battery);
        transport.setSerialNumber("SN" + String.format("%06d", id));
        transport.setLastMaintenanceDate(LocalDateTime.now().minusDays((long) (Math.random() * 30)));

        return transport;
    }

    private String generateModelName(TransportType type, long id) {
        return switch (type) {
            case SCOOTER -> "Xiaomi Mi " + (id % 5 + 1);
            case E_SCOOTER -> "Ninebot ES" + (id % 3 + 1);
            case BICYCLE -> "Trek FX " + (id % 4 + 1);
            case E_BIKE -> "RadRunner " + (id % 2 + 1);
            case MOTORCYCLE -> "Honda CB" + (100 + id % 50);
        };
    }

    private boolean isElectricType(TransportType type) {
        return type == TransportType.E_SCOOTER || type == TransportType.E_BIKE;
    }
}
