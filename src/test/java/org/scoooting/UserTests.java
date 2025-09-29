package org.scoooting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.scoooting.controllers.UserController;
import org.scoooting.dto.UserDTO;
import org.scoooting.dto.common.PageResponseDTO;
import org.scoooting.dto.request.UpdateUserRequestDTO;
import org.scoooting.dto.request.UserRegistrationRequestDTO;
import org.scoooting.dto.response.UserResponseDTO;
import org.scoooting.entities.User;
import org.scoooting.exceptions.common.DataNotFoundException;
import org.scoooting.exceptions.user.UserAlreadyExistsException;
import org.scoooting.exceptions.user.UserNotFoundException;
import org.scoooting.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserTests {

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    private final String testName = "user";
    private final String testEmail = "user1@example.com";
    private final String testPassword = "passHash";

    @BeforeAll
    void setUp() {
        userRepository.save(User.builder()
                        .id(1L)
                        .name(testName)
                        .email(testEmail)
                        .passwordHash(testPassword)
                        .roleId(1L)
                        .cityId(1L)
                        .bonuses(0)
                .build());

    }

    @Test
    void registerUserTest() {
        UserResponseDTO userResponseDTO = userController.registerUser(new UserRegistrationRequestDTO(
                "user2@example.com", testName, testPassword, "SPB")).getBody();
        User user = userRepository.findById(userResponseDTO.id()).orElseThrow();
        assertEquals(userResponseDTO.id(), user.getId());
        userRepository.delete(user);
    }

    @Test
    void registerUserExistsTest() {
        UserRegistrationRequestDTO requestDTO = new UserRegistrationRequestDTO(
                testEmail, testName, testPassword, "SPB");
        Exception exception = assertThrows(UserAlreadyExistsException.class,
                () -> userController.registerUser(requestDTO));

        assertEquals("User with email already exists", exception.getMessage());
    }

    @Test
    void getUsersTest() {
        PageResponseDTO<?> pageResponseDTO = userController.getUsers(testEmail, testName, 0, 50).getBody();
        assertEquals(50, pageResponseDTO.size());
    }

    @Test
    void getCurrentTest() {
        UserResponseDTO user = userController.getUserById(1L).getBody();
        assertEquals(1, user.id());

        user = userController.getUserByEmail(testEmail).getBody();
        assertEquals(testEmail, user.email());
    }

    @Test
    void getUserNotFoundTest() {
        Exception exception = assertThrows(UserNotFoundException.class, () -> userController.getUserById(-1L));
        assertEquals("User not found", exception.getMessage());

        exception = assertThrows(UserNotFoundException.class,
                () -> userController.getUserByEmail("error@example.com"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateUserTest() {
        userController.updateUser(1L, new UpdateUserRequestDTO("update_user", "MSK", 100));
        UserResponseDTO user = userController.getUserById(1L).getBody();
        assertAll(
                () -> assertEquals("update_user", user.name()),
                () -> assertEquals("MSK", user.cityName()),
                () -> assertEquals(100, user.bonuses())
        );
    }

    @Test
    void updateUserErrorsTest() {
        Exception exception = assertThrows(UserNotFoundException.class, () -> userController
                .updateUser(-1L, new UpdateUserRequestDTO("update_user", "MSK", 100))
        );

        assertEquals("User not found", exception.getMessage());
        exception = assertThrows(DataNotFoundException.class, () -> userController
                .updateUser(1L, new UpdateUserRequestDTO("update_user", "JJIO23", 100))
        );
        assertEquals("City not found", exception.getMessage());
    }

    @Test
    void deleteUserTest() {
        User user = userRepository.save(User.builder()
                .name(testName)
                .email("user2@example.com")
                .passwordHash(testPassword)
                .roleId(1L)
                .cityId(1L)
                .bonuses(0)
                .build());

        userController.deleteUser(user.getId());
        Exception exception = assertThrows(UserNotFoundException.class, () -> userController
                .updateUser(user.getId(), new UpdateUserRequestDTO("update_user", "SPB", 100))
        );

        assertEquals("User not found", exception.getMessage());
    }
}
