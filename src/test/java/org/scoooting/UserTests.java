package org.scoooting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.scoooting.controllers.UserController;
import org.scoooting.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class UserTests {

    @Autowired
    private UserController userController;

    @ParameterizedTest
    @CsvSource({
            "0, 50",
            "50, 50",
            "100, 3"
    })
    void getUsersTest(int offset, int expectedSize) {
        List<?> users = (List<?>) userController.getUsers(50, offset).getBody();
        assertEquals(expectedSize, users.size());
    }

    @Test
    void getUserTest() {
        UserDTO user = (UserDTO) userController.getUserById(1L).getBody();
        assertEquals(1, user.id());

        user = (UserDTO) userController.findUserByEmail("user1@example.com").getBody();
        assertEquals("user1@example.com", user.email());
    }

    @Test
    void getUserNotFoundTest() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userController.getUserById(-1L));
        assertEquals("User not found", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class,
                () -> userController.findUserByEmail("efef@example.com"));
        assertEquals("User not found", exception.getMessage());
    }
}
