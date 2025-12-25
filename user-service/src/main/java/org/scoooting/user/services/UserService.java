package org.scoooting.user.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.config.JwtService;
import org.scoooting.user.dto.common.PageResponseDTO;
import org.scoooting.user.dto.request.*;
import org.scoooting.user.dto.JwtDto;
import org.scoooting.user.dto.response.UserResponseDTO;
import org.scoooting.user.entities.City;
import org.scoooting.user.entities.RefreshToken;
import org.scoooting.user.entities.User;
import org.scoooting.user.entities.UserRole;
import org.scoooting.user.exceptions.InvalidRefreshTokenException;
import org.scoooting.user.exceptions.common.DataNotFoundException;
import org.scoooting.user.exceptions.UserAlreadyExistsException;
import org.scoooting.user.exceptions.UserNotFoundException;
import org.scoooting.user.mappers.UserMapper;
import org.scoooting.user.repositories.CityRepository;
import org.scoooting.user.repositories.RefreshTokenRepository;
import org.scoooting.user.repositories.UserRepository;
import org.scoooting.user.repositories.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String addRefreshToken(User user) {
        String roleName = roleRepository.findById(user.getRoleId())
                .map(UserRole::getName)
                .orElse("USER");
        JwtDto jwtDto = jwtService.generateAuthToken(user.getId(), user.getEmail(), roleName);
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(user.getId());
        if (optionalRefreshToken.isEmpty()) {
            refreshTokenRepository.insert(user.getId(), jwtDto.refreshToken());
        } else {
            RefreshToken refreshToken = optionalRefreshToken.get();
            refreshToken.setToken(jwtDto.refreshToken());
            refreshTokenRepository.save(refreshToken);
        }

        return jwtDto.accessToken();
    }

    public String refreshToken(String token) throws UserNotFoundException, InvalidRefreshTokenException {
        if (token == null)
            throw new InvalidRefreshTokenException("Invalid refresh token!");

        String email = jwtService.getEmailFromToken(token);
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(user.getId());
            if (optionalRefreshToken.isPresent()) {
                RefreshToken refreshToken = optionalRefreshToken.get();
                if (jwtService.validateJwtToken(refreshToken.getToken())) {
                    return addRefreshToken(user);
                }

                refreshTokenRepository.delete(refreshToken);
                throw new InvalidRefreshTokenException("Invalid refresh token!");
            }
        }

        throw new UserNotFoundException("User not found!");
    }

    public String registerUser(UserRegistrationRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // Set default USER role
        UserRole userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new DataNotFoundException("USER role not found"));
        user.setRoleId(userRole.getId());

        // Set city if provided
        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

        user = userRepository.save(user);
        return addRefreshToken(userRepository.save(user));
    }

    public String signIn(UserSignInDto userDto) throws UserNotFoundException {
        User user = findByCredentials(userDto);
        return addRefreshToken(user);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> getUsers(String email, String name, int page, int size) {
        int offset = page * size;
        List<User> users = userRepository.findUsersWithFilters(email, name, size, offset);
        long total = userRepository.countUsersWithFilters(email, name);

        List<UserResponseDTO> userDTOs = users.stream().map(this::toResponseDTO).toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(userDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponseDTO(user);
    }


    /**
     * Regular user updates own profile (only name and city)
     */
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

        user = userRepository.save(user);
        return toResponseDTO(user);
    }

    /**
     * Admin can update any field including role, email, bonuses
     */
    public UserResponseDTO adminUpdateUser(Long id, AdminUpdateUserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.email() != null) {
            // Check if email is not taken by another user
            userRepository.findByEmail(request.email()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new UserAlreadyExistsException("Email already taken");
                }
            });
            user.setEmail(request.email());
        }

        if (request.bonuses() != null) {
            user.setBonuses(request.bonuses());
        }

        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

        if (request.roleName() != null) {
            UserRole role = roleRepository.findByName(request.roleName())
                    .orElseThrow(() -> new DataNotFoundException("Role not found"));
            user.setRoleId(role.getId());
        }

        user = userRepository.save(user);
        return toResponseDTO(user);
    }

    /**
     * Support/Admin can add or deduct bonuses with comment for audit
     */
    @Transactional
    public UserResponseDTO addBonuses(Long userId, Integer amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Bonus amount must be non-zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        int currentBonuses = user.getBonuses() != null ? user.getBonuses() : 0;
        int newBonuses = currentBonuses + amount;

        if (newBonuses < 0) {
            throw new IllegalArgumentException("Resulting bonuses cannot be negative. User has " + currentBonuses + " bonuses.");
        }

        user.setBonuses(newBonuses);
        user = userRepository.save(user);

        return toResponseDTO(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO toResponseDTO(User user) {
        String roleName = roleRepository.findById(user.getRoleId())
                .map(UserRole::getName).orElse("UNKNOWN");
        String cityName = user.getCityId() != null ?
                cityRepository.findById(user.getCityId()).map(City::getName).orElse(null) : null;
        return userMapper.toResponseDTO(user, roleName, cityName);
    }

    private User findByCredentials(UserSignInDto userSignInDto) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(userSignInDto.email());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(userSignInDto.password(), user.getPasswordHash()))
                return user;
        }

        throw new UserNotFoundException("Wrong login or password!");
    }

    public String createUserWithRole(UserCreationByAdminRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // Set role from request
        UserRole userRole = roleRepository.findByName(request.roleName())
                .orElseThrow(() -> new DataNotFoundException(request.roleName() + " role not found"));
        user.setRoleId(userRole.getId());

        // Set city if provided
        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

        user.setBonuses(0);
        user = userRepository.save(user);
        return addRefreshToken(user);
    }
}