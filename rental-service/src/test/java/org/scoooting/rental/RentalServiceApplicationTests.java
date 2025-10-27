package org.scoooting.rental;

import org.junit.jupiter.api.BeforeEach;
import org.scoooting.rental.clients.feign.FeignTransportClient;
import org.scoooting.rental.clients.feign.FeignUserClient;
import org.scoooting.rental.repositories.RentalRepository;
import org.scoooting.rental.services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
    private FeignUserClient feignUserClient;

    @MockitoBean
    private FeignTransportClient transportClient;

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
