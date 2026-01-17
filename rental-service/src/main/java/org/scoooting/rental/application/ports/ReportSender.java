package org.scoooting.rental.application.ports;

import org.scoooting.rental.application.dto.RentalResponseDTO;
import reactor.core.publisher.Mono;

public interface ReportSender {

    Mono<Void> send(RentalResponseDTO rental, Long userId, String name, String email);
}
