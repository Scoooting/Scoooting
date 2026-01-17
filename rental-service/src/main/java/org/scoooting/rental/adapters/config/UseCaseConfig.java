package org.scoooting.rental.adapters.config;

import org.scoooting.rental.adapters.message.feign.resilient.ResilientFileClient;
import org.scoooting.rental.adapters.message.kafka.TransportPublisher;
import org.scoooting.rental.adapters.message.kafka.UserPublisher;
import org.scoooting.rental.application.mappers.RentalMapper;
import org.scoooting.rental.application.ports.NotificationSender;
import org.scoooting.rental.application.ports.ReportSender;
import org.scoooting.rental.application.ports.TransportClient;
import org.scoooting.rental.application.ports.UserClient;
import org.scoooting.rental.application.usecase.rentals.*;
import org.scoooting.rental.application.usecase.sendings.SendNotificationUseCase;
import org.scoooting.rental.application.usecase.sendings.SendReportUseCase;
import org.scoooting.rental.domain.repositories.RentalRepository;
import org.scoooting.rental.domain.repositories.RentalStatusRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CancelRentalUseCase cancelRentalUseCase(RentalRepository rentalRepository,
                                                   RentalStatusRepository rentalStatusRepository,
                                                   TransportClient transportClient,
                                                   TransportPublisher transportPublisher,
                                                   RentalMapper rentalMapper) {
        return new CancelRentalUseCase(rentalRepository, rentalStatusRepository,
                transportClient, transportPublisher, rentalMapper);
    }

    @Bean
    public EndRentalUseCase endRentalUseCase(RentalRepository rentalRepository,
                                             RentalStatusRepository rentalStatusRepository,
                                             TransportClient transportClient,
                                             TransportPublisher transportPublisher,
                                             UserPublisher userPublisher,
                                             ResilientFileClient fileClient,
                                             RentalMapper rentalMapper) {
        return new EndRentalUseCase(rentalRepository, rentalStatusRepository,
                transportClient, transportPublisher, userPublisher,rentalMapper, fileClient);
    }

    @Bean
    public ForceEndRentalUseCase forceEndRentalUseCase(RentalRepository rentalRepository,
                                                       RentalStatusRepository rentalStatusRepository,
                                                       TransportClient transportClient,
                                                       UserClient userClient,
                                                       TransportPublisher transportPublisher,
                                                       UserPublisher userPublisher,
                                                       RentalMapper rentalMapper) {
        return new ForceEndRentalUseCase(rentalRepository, rentalStatusRepository,
                transportClient, userClient, transportPublisher, userPublisher, rentalMapper);
    }

    @Bean
    public GetActiveRentalUseCase getActiveRentalUseCase(RentalRepository rentalRepository,
                                                         RentalMapper rentalMapper) {
        return new GetActiveRentalUseCase(rentalRepository, rentalMapper);
    }

    @Bean
    public RentalHistoryUseCase rentalHistoryUseCase(RentalRepository rentalRepository,
                                                     RentalMapper rentalMapper) {
        return new RentalHistoryUseCase(rentalRepository, rentalMapper);
    }

    @Bean
    public StartRentalUseCase startRentalUseCase(RentalRepository rentalRepository,
                                                 RentalStatusRepository rentalStatusRepository,
                                                 TransportClient transportClient,
                                                 TransportPublisher transportPublisher,
                                                 RentalMapper rentalMapper) {
        return new StartRentalUseCase(rentalRepository, rentalStatusRepository,
                transportClient, transportPublisher, rentalMapper);
    }

    @Bean
    public SendNotificationUseCase sendNotificationUseCase(NotificationSender notificationSender) {
        return new SendNotificationUseCase(notificationSender);
    }

    @Bean
    public SendReportUseCase sendReportUseCase(ReportSender reportSender) {
        return new SendReportUseCase(reportSender);
    }
}