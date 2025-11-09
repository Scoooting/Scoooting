package org.scoooting.user.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.dto.common.PageResponseDTO;
import org.scoooting.user.dto.request.UpdateUserRequestDTO;
import org.scoooting.user.dto.request.UserSignInDto;
import org.scoooting.user.dto.response.UserResponseDTO;
import org.scoooting.user.entities.City;
import org.scoooting.user.entities.User;
import org.scoooting.user.entities.UserRole;
import org.scoooting.user.exceptions.common.DataNotFoundException;
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
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
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

    @Transactional
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
