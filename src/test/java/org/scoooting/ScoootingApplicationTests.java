package org.scoooting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.scoooting.dto.common.PageResponseDTO;
import org.scoooting.dto.response.UserResponseDTO;
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
            "0, 50, 50",   // page 0, size 50, expected 50
            "1, 50, 50",   // page 1, size 50, expected 50
            "2, 3, 3"      // page 2, size 3, expected 3
    })
    void pagingTest(int page, int size, int expectedSize) {
        PageResponseDTO<UserResponseDTO> result = userService.getUsers(null, null, page, size);
        assertEquals(expectedSize, result.content().size());
    }

    @Test
    void searchByEmailTest() {
        PageResponseDTO<UserResponseDTO> result = userService.getUsers("admin@", null, 0, 20);
        assertTrue(result.content().size() > 0);
        assertTrue(result.content().get(0).email().contains("admin"));
    }

    @Test
    void searchByNameTest() {
        PageResponseDTO<UserResponseDTO> result = userService.getUsers(null, "admin", 0, 20);
        assertTrue(result.content().size() > 0);
    }

    @Test
    void paginationMetadataTest() {
        PageResponseDTO<UserResponseDTO> result = userService.getUsers(null, null, 0, 10);

        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertTrue(result.totalElements() > 0);
        assertTrue(result.first());
    }
}