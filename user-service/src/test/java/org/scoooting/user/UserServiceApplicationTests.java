package org.scoooting.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.scoooting.user.config.JwtService;
import org.scoooting.user.dto.common.PageResponseDTO;
import org.scoooting.user.dto.request.*;
import org.scoooting.user.dto.response.UserResponseDTO;
import org.scoooting.user.entities.City;
import org.scoooting.user.entities.RefreshToken;
import org.scoooting.user.entities.User;
import org.scoooting.user.exceptions.InvalidRefreshTokenException;
import org.scoooting.user.exceptions.UserAlreadyExistsException;
import org.scoooting.user.exceptions.UserNotFoundException;
import org.scoooting.user.exceptions.common.DataNotFoundException;
import org.scoooting.user.repositories.RefreshTokenRepository;
import org.scoooting.user.repositories.UserRepository;
import org.scoooting.user.repositories.UserRoleRepository;
import org.scoooting.user.services.CityService;
import org.scoooting.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    private final String testName = "user";
    private final String testEmail = "user1@example.com";
    private final String testPassword = "passHash";
    private final String testCity = "SPB";
    private final String testRole = "ANALYST";
    private User testUser = new User();

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .name(testName)
                .email(testEmail)
                .passwordHash(passwordEncoder.encode(testPassword))
                .roleId(1L)
                .cityId(1L)
                .bonuses(0)
                .build());
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
        userService.updateUser(testUser.getId(), new UpdateUserRequestDTO("update_user", "MSK"));
        UserResponseDTO userDto = userService.findUserById(testUser.getId());
        assertAll(
                () -> assertEquals("update_user", userDto.name()),
                () -> assertEquals("MSK", userDto.cityName())
        );
    }

    @Test
    void updateUserErrorsTest() {
        Exception exception = assertThrows(UserNotFoundException.class, () -> userService
                .updateUser(-1L, new UpdateUserRequestDTO("update_user", "MSK"))
        );

        assertEquals("User not found", exception.getMessage());
        exception = assertThrows(DataNotFoundException.class, () -> userService
                .updateUser(testUser.getId(), new UpdateUserRequestDTO(testName, "JJIO23"))
        );
        assertEquals("City not found", exception.getMessage());
    }

    @Test
    void deleteUserTest() {
        userService.deleteUser(testUser.getId());
        Exception exception = assertThrows(UserNotFoundException.class, () -> userService
                .deleteUser(testUser.getId())
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

    @Test
    void signInTest() {
        String token = userService.signIn(new UserSignInDto(testEmail, testPassword));
        String actualEmail = jwtService.getEmailFromToken(token);
        assertEquals(testEmail, actualEmail);

        User user = userRepository.findByEmail(testEmail).orElseThrow();
        String refreshToken = refreshTokenRepository.findById(user.getId()).orElseThrow().getToken();
        assertTrue(jwtService.validateJwtToken(refreshToken));
    }

    @Test
    void signInTestWrongData() {
        Exception exception = assertThrows(UserNotFoundException.class, () ->
                userService.signIn(new UserSignInDto("blabla@example.com", testPassword)));
        assertEquals("Wrong login or password!", exception.getMessage());

        exception = assertThrows(UserNotFoundException.class, () ->
                userService.signIn(new UserSignInDto(testEmail, "wrongPass")));
        assertEquals("Wrong login or password!", exception.getMessage());
    }

    @Test
    void registerUserTest() {
        String expectedEmail = "new@example.com";
        String token = userService.registerUser(new UserRegistrationRequestDTO
                (expectedEmail, testName, testPassword, "SPB"));
        String actualEmail = jwtService.getEmailFromToken(token);
        assertEquals(expectedEmail, actualEmail);

        User user = userRepository.findByEmail(expectedEmail).orElseThrow();
        String refreshToken = refreshTokenRepository.findById(user.getId()).orElseThrow().getToken();
        assertTrue(jwtService.validateJwtToken(refreshToken));

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(
                new UserRegistrationRequestDTO(expectedEmail, testName, testPassword, "SPB")));
        assertEquals("User with email already exists", exception.getMessage());
    }

    @Test
    void registerUserDataNotFoundTest() {
        Exception exception = assertThrows(DataNotFoundException.class, () -> userService.registerUser(
                new UserRegistrationRequestDTO("new@example.com", testName, "1234", "STRANGE_CITY")));
        assertEquals("City not found", exception.getMessage());
    }

    @Test
    void refreshTokenTest() throws InterruptedException {
        String token = userService.signIn(new UserSignInDto(testEmail, testPassword));
        Thread.sleep(1000);
        String newToken = userService.refreshToken(token);
        assertAll(
                () -> assertTrue(jwtService.validateJwtToken(newToken)),
                () -> assertNotEquals(token, newToken),
                () -> assertEquals(testEmail, jwtService.getEmailFromToken(newToken))
        );
    }

    @Test
    void refreshTokenInvalidTest() {
        String token = userService.signIn(new UserSignInDto(testEmail, testPassword));
        RefreshToken refreshToken = refreshTokenRepository.findById(testUser.getId()).orElseThrow();
        refreshToken.setToken("invalid");
        refreshTokenRepository.save(refreshToken);

        Exception exception = assertThrows(InvalidRefreshTokenException.class, () -> userService.refreshToken(null));
        assertEquals("Invalid refresh token!", exception.getMessage());

        exception = assertThrows(InvalidRefreshTokenException.class, () -> userService.refreshToken(token));
        assertEquals("Invalid refresh token!", exception.getMessage());
    }

    @Test
    void adminUpdateUserTest() {
        String newName = "newName";
        String newEmail = "new@example.com";
        String newCity = "MSK";
        int newBonuses = 1000;
        String newRole = "ANALYST";

        UserResponseDTO responseDTO = userService.adminUpdateUser(testUser.getId(),
                new AdminUpdateUserRequestDTO(newName, newEmail, newCity, newBonuses, newRole));

        assertAll(
                () -> assertEquals(newName, responseDTO.name()),
                () -> assertEquals(newEmail, responseDTO.email()),
                () -> assertEquals(newCity, responseDTO.cityName()),
                () -> assertEquals(newBonuses, responseDTO.bonuses()),
                () -> assertEquals(newRole, responseDTO.role())
        );
        User user = userRepository.findByEmail(newEmail).orElseThrow();
        assertAll(
                () -> assertEquals(newName, user.getName()),
                () -> assertEquals(newEmail, user.getEmail()),
                () -> assertEquals(newCity, cityService.getCityById(user.getCityId())),
                () -> assertEquals(newBonuses, user.getBonuses()),
                () -> assertEquals(newRole, userRoleRepository.findById(user.getRoleId()).orElseThrow().getName())
        );
    }

    @Test
    void addBonusesTest() {
        userService.addBonuses(testUser.getId(), 200);
        assertEquals(200, userRepository.findById(testUser.getId()).orElseThrow().getBonuses());
    }

    @Test
    void createUserWithRoleTest() {
        String createEmail = "new@example.com";
        userService.createUserWithRole(
                new UserCreationByAdminRequestDTO(createEmail, testName, testPassword, testRole, testCity));

        User user = userRepository.findByEmail(createEmail).orElseThrow();
        assertAll(
                () -> assertEquals(testName, user.getName()),
                () -> assertEquals(createEmail, user.getEmail()),
                () -> assertEquals(testCity, cityService.getCityById(user.getCityId())),
                () -> assertEquals(0, user.getBonuses()),
                () -> assertEquals(testRole, userRoleRepository.findById(user.getRoleId()).orElseThrow().getName())
        );
    }
}