package org.scoooting.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scoooting.user.dto.common.PageResponseDTO;
import org.scoooting.user.dto.request.UpdateUserRequestDTO;
import org.scoooting.user.dto.request.UserRegistrationRequestDTO;
import org.scoooting.user.dto.response.UserResponseDTO;
import org.scoooting.user.entities.User;
import org.scoooting.user.exceptions.UserAlreadyExistsException;
import org.scoooting.user.exceptions.UserNotFoundException;
import org.scoooting.user.exceptions.common.DataNotFoundException;
import org.scoooting.user.repositories.UserRepository;
import org.scoooting.user.services.CityService;
import org.scoooting.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class UserServiceApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private CityService cityService;

    @Autowired
    private UserRepository userRepository;

    private final String testName = "user";
    private final String testEmail = "user1@example.com";
    private final String testPassword = "passHash";
    private User testUser = new User();

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .name(testName)
                .email(testEmail)
                .passwordHash(testPassword)
                .roleId(1L)  // ⬅️ Убедитесь что роль существует
                .cityId(1L)  // ⬅️ Убедитесь что город существует
                .bonuses(0)
                .build());
    }

    @Test
    void registerUserTest() {
        UserResponseDTO userResponseDTO = userService.registerUser(new UserRegistrationRequestDTO(
                "user2@example.com", testName, testPassword, "SPB"));
        User user = userRepository.findById(userResponseDTO.id()).orElseThrow();
        assertEquals(userResponseDTO.id(), user.getId());
        userRepository.delete(user);
    }

    @Test
    void registerUserExistsTest() {
        UserRegistrationRequestDTO requestDTO = new UserRegistrationRequestDTO(
                testEmail, testName, testPassword, "SPB");
        Exception exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(requestDTO));

        assertEquals("User with email already exists", exception.getMessage());
    }

    @Test
    void getUsersTest() {
        PageResponseDTO<?> pageResponseDTO = userService.getUsers(testEmail, testName, 0, 50);
        assertEquals(1, pageResponseDTO.content().size());
    }

    @Test
    void getCurrentTest() {
        UserResponseDTO userDto = userService.findUserById(testUser.getId());
        assertEquals(testUser.getId(), userDto.id());

        userDto = userService.findUserByEmail(testEmail);
        assertEquals(testEmail, userDto.email());
    }

    @Test
    void getUserNotFoundTest() {
        Exception exception = assertThrows(UserNotFoundException.class, () -> userService.findUserById(-1L));
        assertEquals("User not found", exception.getMessage());

        exception = assertThrows(UserNotFoundException.class,
                () -> userService.findUserByEmail("error@example.com"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateUserTest() {
        userService.updateUser(testUser.getId(), new UpdateUserRequestDTO("update_user", "MSK", 100));
        UserResponseDTO userDto = userService.findUserById(testUser.getId());
        assertAll(
                () -> assertEquals("update_user", userDto.name()),
                () -> assertEquals("MSK", userDto.cityName()),
                () -> assertEquals(100, userDto.bonuses())
        );
    }

    @Test
    void updateUserErrorsTest() {
        System.out.println(testUser);
        Exception exception = assertThrows(UserNotFoundException.class, () -> userService
                .updateUser(-1L, new UpdateUserRequestDTO("update_user", "MSK", 100))
        );

        assertEquals("User not found", exception.getMessage());
        exception = assertThrows(DataNotFoundException.class, () -> userService
                .updateUser(testUser.getId(), new UpdateUserRequestDTO(testName, "JJIO23", 100))
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

        userService.deleteUser(user.getId());
        Exception exception = assertThrows(UserNotFoundException.class, () -> userService
                .updateUser(user.getId(), new UpdateUserRequestDTO(testName, "SPB", 100))
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getCityByIdTest() {
        String city = cityService.getCityById(testUser.getCityId());
        assertEquals(city, "SPB");

        Exception exception = assertThrows(DataNotFoundException.class, () -> cityService.getCityById(-1L));
        assertEquals("City not found", exception.getMessage());
    }
}
