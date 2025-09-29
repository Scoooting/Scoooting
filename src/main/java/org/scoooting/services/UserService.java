package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.common.PageResponseDTO;
import org.scoooting.dto.request.UpdateUserRequestDTO;
import org.scoooting.dto.request.UserRegistrationRequestDTO;
import org.scoooting.dto.response.UserResponseDTO;
import org.scoooting.entities.City;
import org.scoooting.entities.User;
import org.scoooting.entities.UserRole;
import org.scoooting.exceptions.common.DataNotFoundException;
import org.scoooting.exceptions.user.UserAlreadyExistsException;
import org.scoooting.exceptions.user.UserNotFoundException;
import org.scoooting.mappers.UserMapper;
import org.scoooting.repositories.CityRepository;
import org.scoooting.repositories.UserRepository;
import org.scoooting.repositories.UserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO registerUser(UserRegistrationRequestDTO request) {
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
        return toResponseDTO(user);
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

    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.name() != null) user.setName(request.name());
        if (request.bonuses() != null) user.setBonuses(request.bonuses());

        if (request.cityName() != null) {
            City city = cityRepository.findByName(request.cityName())
                    .orElseThrow(() -> new DataNotFoundException("City not found"));
            user.setCityId(city.getId());
        }

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
}
