package org.scoooting.rental.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.config.UserPrincipal;
import org.scoooting.rental.dto.kafka.ReportDataDTO;
import org.scoooting.rental.dto.response.RentalResponseDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> Mono<Void> sendMessage(String topic, T object) {
        return Mono.fromFuture(kafkaTemplate.send(topic, object)).then();
    }

    public Mono<Void> sendReport(RentalResponseDTO rental, UserPrincipal userPrincipal) {
        return sendMessage("reports-data", ReportDataDTO.builder()
                .rentalId(rental.getId())
                .userId(userPrincipal.getUserId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .transport(rental.getTransportType())
                .startTime(rental.getStartTime().getEpochSecond())
                .endTime(rental.getEndTime().getEpochSecond())
                .durationMinutes(rental.getDurationMinutes())
                .status(rental.getStatus())
                .totalCost(rental.getTotalCost().intValue())
                .build());
    }
}
