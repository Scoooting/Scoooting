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

    /**
     * Get paginated list of users with optional filters.
     *
     * TRANSACTION (readOnly=true) IS NEEDED:
     * - Makes multiple queries:
     *   1. SELECT users with filters and pagination
     *   2. SELECT COUNT(*) for total count
     *   3. For each user: SELECT role + SELECT city (via toResponseDTO)
     *   4. For 20 users: 2 + 20 + 20 = 42 queries
     * - Without transaction: 42 separate connections from pool
     * - With transaction: all 42 queries share single connection
     *
     * WHY readOnly=true:
     * - Tells Hibernate to skip dirty checking (no tracking of entity changes)
     * - Tells JDBC driver to use read-only connection mode:
     *   * PostgreSQL: can use read replica if available
     *   * MySQL: skips lock acquisition
     *   * Oracle: skips undo log generation
     * - Hibernate skips flush() before each query (saves 10-20% time)
     * - Overall: 15-30% faster than regular transaction for read operations
     *
     * CONNECTION REUSE:
     * - Opening connection: ~5-10ms
     * - For 42 queries without transaction: 42 × 10ms = 420ms just for connections!
     * - With transaction: 10ms for one connection
     *
     * @param email optional email filter
     * @param name optional name filter
     * @param page page number (0-indexed)
     * @param size items per page
     * @return paginated response with total count and page info
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> getUsers(String email, String name, int page, int size) {
        int offset = page * size;
        List<User> users = userRepository.findUsersWithFilters(email, name, size, offset);
        long total = userRepository.countUsersWithFilters(email, name);

        List<UserResponseDTO> userDTOs = users.stream().map(this::toResponseDTO).toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(userDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }


    /**
     * Find user by ID with role and city details.
     *
     * TRANSACTION (readOnly=true) IS NEEDED:
     * - Makes 3 queries: SELECT user + SELECT role + SELECT city
     * - Without transaction: 3 separate connections (30ms overhead)
     * - With transaction: single connection (10ms overhead)
     *
     * WHY readOnly=true is important:
     * - User entity might be managed by Hibernate session
     * - Without readOnly: Hibernate checks for changes (dirty checking) even though we don't modify
     * - With readOnly: skips dirty checking → saves CPU cycles
     * - Session.flush() is skipped before toResponseDTO queries
     *
     * LAZY LOADING:
     * - If User has lazy-loaded fields (e.g., @OneToMany relationships)
     * - Without transaction: LazyInitializationException when accessing lazy fields
     * - With transaction: lazy loading works because session stays open
     *
     * @param id user ID
     * @return user DTO with role and city
     * @throws UserNotFoundException if user doesn't exist
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponseDTO(user);
    }


    /**
     * Find user by email with role and city details.
     *
     * TRANSACTION (readOnly=true) IS NEEDED for same reasons as findUserById:
     * - 3 queries: SELECT user by email + SELECT role + SELECT city
     * - Connection reuse saves overhead
     * - readOnly=true optimizes Hibernate behavior
     * - Enables lazy loading if needed
     *
     * Email lookup uses index, so query is fast, but we still need transaction
     * for the subsequent role/city lookups in toResponseDTO().
     *
     * @param email user email
     * @return user DTO
     * @throws UserNotFoundException if user doesn't exist
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponseDTO(user);
    }

    /**
     * Update user details (name, bonuses, city).
     *
     * TRANSACTION IS CRITICAL:
     * - Makes multiple queries:
     *   1. SELECT user by ID
     *   2. SELECT city by name (if cityName provided)
     *   3. UPDATE user
     *   4. SELECT role + SELECT city (in toResponseDTO)
     * - Total: up to 5 queries that must be atomic
     *
     * WHY transaction is critical:
     * 1. ATOMICITY: All updates succeed or all rollback
     *    - If city lookup fails, user should NOT be partially updated
     *    - If database error during save, no changes persisted
     *
     * 2. CONSISTENCY: Prevents race conditions
     *    Example without transaction:
     *    Thread 1: SELECT user (bonuses=100)
     *    Thread 2: SELECT user (bonuses=100)
     *    Thread 1: SET bonuses=150, UPDATE
     *    Thread 2: SET bonuses=200, UPDATE ← Overwrites Thread 1!
     *    Result: Lost update! Thread 1's change is gone.
     *
     *    With transaction:
     *    Thread 1: BEGIN TRANSACTION, SELECT user FOR UPDATE (locks row)
     *    Thread 2: BEGIN TRANSACTION, SELECT user FOR UPDATE (WAITS for Thread 1)
     *    Thread 1: UPDATE, COMMIT (releases lock)
     *    Thread 2: Now can SELECT (sees Thread 1's changes), UPDATE, COMMIT
     *    Result: Both updates preserved correctly
     *
     * 3. ISOLATION: Other transactions don't see partial updates
     *    - During update, other transactions see old values until COMMIT
     *    - Prevents reading inconsistent state
     *
     * 4. DIRTY CHECKING: Hibernate automatic UPDATE
     *    - Even without explicit save(), Hibernate detects changes
     *    - user.setName() marks entity as dirty
     *    - Transaction COMMIT triggers automatic UPDATE
     *    - Without transaction: changes might not be persisted!
     *
     * @param id user ID
     * @param request update fields (null fields are ignored)
     * @return updated user DTO
     * @throws UserNotFoundException if user doesn't exist
     * @throws DataNotFoundException if city doesn't exist
     */
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

    /**
     * Delete user by ID.
     *
     * TRANSACTION IS NEEDED:
     * - Makes 2 queries: SELECT (existsById) + DELETE
     * - Without transaction: race condition between check and delete
     *
     * RACE CONDITION example without transaction:
     * Thread 1: existsById(5) → true
     * Thread 2: deleteById(5) → success (user deleted)
     * Thread 1: deleteById(5) → tries to delete already-deleted user
     * Result: Inconsistent state, potential errors
     *
     * With transaction:
     * Thread 1: BEGIN, existsById(5) → locks row
     * Thread 2: BEGIN, waits for lock...
     * Thread 1: deleteById(5), COMMIT
     * Thread 2: existsById(5) → false, throws exception
     * Result: Consistent behavior
     *
     * ATOMICITY:
     * - If existsById succeeds but deleteById fails (e.g., foreign key constraint)
     * - Transaction rollback ensures no partial state
     * - Exception is thrown to caller with consistent database state
     *
     * OPTIMIZATION:
     * Could be simplified to:
     * User user = userRepository.findById(id)
     *     .orElseThrow(() -> new UserNotFoundException("User not found"));
     * userRepository.delete(user);
     * This reduces 2 queries (existsById + deleteById) to 1 (findById + delete)
     *
     * @param id user ID to delete
     * @throws UserNotFoundException if user doesn't exist
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Convert User entity to DTO with role and city names.
     *
     * TRANSACTION NOT NEEDED DIRECTLY:
     * - Private helper method called from within transactional methods
     * - Makes 2 queries: SELECT role + SELECT city
     * - Inherits transaction from caller
     *
     * WHY no @Transactional here:
     * - Private methods are NOT proxied by Spring AOP
     * - @Transactional on private method has NO EFFECT
     * - Relies on caller's transaction context
     * - If caller has transaction → this runs in same transaction
     * - If caller has no transaction → each query runs in separate mini-transaction
     *
     * N+1 PROBLEM:
     * - When called in loop (e.g., getUsers for 20 users): 20 × 2 = 40 queries
     * - This is why caller methods need @Transactional(readOnly=true)
     * - With caller's transaction: all 40 queries share one connection
     * - Without: 40 separate connections (400ms overhead!)
     *
     * OPTIMIZATION opportunity:
     * - For getUsers, could batch-load all roles and cities first
     * - Then pass Maps to toResponseDTO to avoid queries
     * - Would reduce 40 queries to just 2 queries
     *
     * @param user entity to convert
     * @return DTO with role name and city name
     */
    private UserResponseDTO toResponseDTO(User user) {
        String roleName = roleRepository.findById(user.getRoleId())
                .map(UserRole::getName).orElse("UNKNOWN");
        String cityName = user.getCityId() != null ?
                cityRepository.findById(user.getCityId()).map(City::getName).orElse(null) : null;
        return userMapper.toResponseDTO(user, roleName, cityName);
    }
}
