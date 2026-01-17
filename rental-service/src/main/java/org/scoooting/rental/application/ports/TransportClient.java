package org.scoooting.rental.application.ports;

import org.scoooting.rental.adapters.message.feign.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.application.dto.TransportResponseDTO;

public interface TransportClient {
    Long getTransportStatusId(String name);
    TransportResponseDTO getTransport(Long id);
}
