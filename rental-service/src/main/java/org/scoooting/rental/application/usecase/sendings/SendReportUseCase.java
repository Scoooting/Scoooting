package org.scoooting.rental.application.usecase.sendings;

import lombok.RequiredArgsConstructor;
import org.scoooting.rental.adapters.security.UserPrincipal;
import org.scoooting.rental.application.ports.ReportSender;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SendReportUseCase {

    private final ReportSender reportSender;

    public Mono<Void> sendReport(RentalResponseDTO rental, UserPrincipal userPrincipal) {
        return reportSender.send(rental, userPrincipal.getUserId(), userPrincipal.getUsername(),
                userPrincipal.getEmail());
    }
}
