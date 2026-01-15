package org.scoooting.rental.adapters.config;

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
                                                   RentalMapper rentalMapper) {
        return new CancelRentalUseCase(rentalRepository, rentalStatusRepository, transportClient, rentalMapper);
    }

    @Bean
    public EndRentalUseCase endRentalUseCase(RentalRepository rentalRepository,
                                             RentalStatusRepository rentalStatusRepository,
                                             TransportClient transportClient,
                                             UserClient userClient,
                                             RentalMapper rentalMapper) {
        return new EndRentalUseCase(rentalRepository, rentalStatusRepository, transportClient, userClient, rentalMapper);
    }

    @Bean
    public ForceEndRentalUseCase forceEndRentalUseCase(RentalRepository rentalRepository,
                                                       RentalStatusRepository rentalStatusRepository,
                                                       TransportClient transportClient,
                                                       UserClient userClient,
                                                       RentalMapper rentalMapper) {
        return new ForceEndRentalUseCase(rentalRepository, rentalStatusRepository,
                transportClient, userClient, rentalMapper);
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
                                                 RentalMapper rentalMapper) {
        return new StartRentalUseCase(rentalRepository, rentalStatusRepository, transportClient, rentalMapper);
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
