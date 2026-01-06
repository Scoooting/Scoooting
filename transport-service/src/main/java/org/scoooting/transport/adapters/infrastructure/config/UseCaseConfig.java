package org.scoooting.transport.adapters.infrastructure.config;

import org.scoooting.transport.application.ports.BatteryEventPublisher;
import org.scoooting.transport.application.ports.UserClient;
import org.scoooting.transport.application.usecase.*;
import org.scoooting.transport.domain.mappers.TransportMapper;
import org.scoooting.transport.domain.repositories.TransportRepository;
import org.scoooting.transport.domain.repositories.TransportStatusRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class UseCaseConfig {

    @Bean
    public TransportFindUseCase transportUseCase(TransportRepository transportRepository,
                                                 TransactionalOperator transactionalOperator,
                                                 ToResponseDto toResponseDto) {
        return new TransportFindUseCase(transportRepository, transactionalOperator, toResponseDto);
    }

    @Bean
    public ToResponseDto toResponseDto(UserClient userClient, TransportStatusRepository transportStatusRepository,
                                       TransportMapper transportMapper) {
        return new ToResponseDto(userClient, transportStatusRepository, transportMapper);
    }

    @Bean
    public TransportUpdateUseCase transportUpdateUseCase(TransportRepository transportRepository,
                                                         TransportStatusRepository transportStatusRepository,
                                                         TransactionalOperator transactionalOperator,
                                                         ToResponseDto toResponseDto
                                                         ) {
        return new TransportUpdateUseCase(transportRepository, transportStatusRepository, transactionalOperator,
                toResponseDto);
    }

    @Bean
    public TransportStatsUseCase transportStatsDto(TransportRepository transportRepository) {
        return new TransportStatsUseCase(transportRepository);
    }

    @Bean
    public BatteryNotificationUseCase batteryNotificationUseCase(BatteryEventPublisher publisher,
                                                                 TransportRepository repository) {
        return new BatteryNotificationUseCase(publisher, repository);
    }

}
