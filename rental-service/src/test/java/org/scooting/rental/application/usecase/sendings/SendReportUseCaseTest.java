package org.scooting.rental.application.usecase.sendings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.rental.adapters.security.UserPrincipal;
import org.scoooting.rental.application.dto.RentalResponseDTO;
import org.scoooting.rental.application.ports.ReportSender;
import org.scoooting.rental.application.usecase.sendings.SendReportUseCase;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendReportUseCaseTest {

    @Mock
    private ReportSender reportSender;

    @InjectMocks
    private SendReportUseCase sendReportUseCase;

    @Test
    void sendReport_Success() {
        // Arrange
        RentalResponseDTO rental = RentalResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .build();

        UserPrincipal principal = new UserPrincipal(
                "testuser", 100L, "test@example.com", "USER"
        );

        when(reportSender.send(any(), anyLong(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sendReportUseCase.sendReport(rental, principal))
                .verifyComplete();

        verify(reportSender).send(rental, 100L, "testuser", "test@example.com");
    }
}