package org.scoooting.rental.adapters.message.kafka;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.message.kafka.dto.ReportDataDTO;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.ports.ReportSender;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReportPublisher implements ReportSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Void> send(RentalResponseDTO rental, Long userId, String name, String email) {
        return Mono.fromFuture(
                kafkaTemplate.send(KafkaConfig.REPORTS_DATA_TOPIC, ReportDataDTO.builder()
                .rentalId(rental.getId())
                .userId(userId)
                .username(name)
                .email(email)
                .transport(rental.getTransportType())
                .startTime(rental.getStartTime().getEpochSecond())
                .endTime(rental.getEndTime().getEpochSecond())
                .durationMinutes(rental.getDurationMinutes())
                .status(rental.getStatus())
                .totalCost(rental.getTotalCost().intValue())
                .build())
        ).then();
    }
}
