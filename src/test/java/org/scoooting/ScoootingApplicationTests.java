package org.scoooting;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.scoooting.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ScoootingApplicationTests {

    @Autowired
    private UserService userService;

    @ParameterizedTest
    @CsvSource({
            "0, 50",
            "1, 50",
            "2, 3"
    })
    void pagingTest(int page, int expectedSize) {
        assertEquals(expectedSize, userService.getPagingUsers(page).size());
    }

}
