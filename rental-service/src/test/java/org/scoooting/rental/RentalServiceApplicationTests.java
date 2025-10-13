package org.scoooting.rental;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scoooting.rental.clients.TransportClient;
import org.scoooting.rental.clients.UserClient;
import org.scoooting.rental.dto.response.TransportResponseDTO;
import org.scoooting.rental.dto.response.UserResponseDTO;
import org.scoooting.rental.repositories.RentalRepository;
import org.scoooting.rental.services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class RentalServiceApplicationTests {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private RentalRepository rentalRepository;

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private TransportClient transportClient;

    @BeforeEach
    void beforeEach() {
        rentalRepository.deleteAll();
    }

//    @Test
//    void startRentalService() {
//        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
//                .id(1L)
//                .email("user1@example.com")
//                .name("user")
//                .role("USER")
//                .cityName("SPB")
//                .bonuses(0)
//                .build();
//
//        TransportResponseDTO transportResponseDTO = TransportResponseDTO.builder()
//                        .id(10L)
//                        .type("")
//        when(userClient.getUserById(1L)).thenReturn(ResponseEntity.ok(userResponseDTO));
//        rentalService.startRental(1L, )
//    }
}
