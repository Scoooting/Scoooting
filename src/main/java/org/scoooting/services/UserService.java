package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.UserDto;
import org.scoooting.entities.User;
import org.scoooting.mappers.UserMapper;
import org.scoooting.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private static final int USERS_LIMIT = 50;

    /**
     * Возвращает список из dto пользователей в размере не более 50.
     * @param page
     * @return
     */
    public List<UserDto> getPagingUsers(int page) {
        Page<User> users = userRepository.findAll(PageRequest.of(page, USERS_LIMIT));
        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }
}
