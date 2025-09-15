package org.scoooting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.scoooting.dto.ScootersDto;
import org.scoooting.entities.Scooter;
import org.scoooting.entities.enums.ScootersStatus;
import org.scoooting.repositories.ScooterRepository;
import org.scoooting.services.ScooterService;
import org.scoooting.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class ScoootingApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ScooterService scooterService;

    @Autowired
    private ScooterRepository scooterRepository;

    @ParameterizedTest
    @CsvSource({
            "0, 50",
            "1, 50",
            "2, 3"
    })
    void pagingTest(int page, int expectedSize) {
        assertEquals(expectedSize, userService.getPagingUsers(page).size());
    }

    @Test
    void getNearestScootersTest() {
        List<Scooter> scooters = List.of(
                new Scooter(1L, "Urent 10A8E", ScootersStatus.FREE, 59.88307F, 30.198868F),
                new Scooter(2L, "Urent 10A8E", ScootersStatus.FREE, 59.8839F, 30.19856F),
                new Scooter(3L, "Urent 10A8E", ScootersStatus.FREE, 59.88102F, 30.1913F),
                new Scooter(4L, "Urent 10A8E", ScootersStatus.FREE, 59.89034F, 30.17839F));

        scooterRepository.saveAll(scooters);
        List<ScootersDto> nearest = scooterService.findNearestScooters(59.88307F, 30.198868F)
                .stream().filter(e -> e.getId() < 5).toList();
        assertEquals(3, nearest.size());
    }

}
