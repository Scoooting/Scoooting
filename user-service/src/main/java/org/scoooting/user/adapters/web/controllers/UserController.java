package org.scoooting.user.adapters.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.user.adapters.security.UserPrincipal;
import org.scoooting.user.adapters.web.mappers.WebCommandMapper;
import org.scoooting.user.adapters.web.dto.AdminUpdateUserRequestDTO;
import org.scoooting.user.adapters.web.dto.UpdateUserRequestDTO;
import org.scoooting.user.adapters.web.dto.UserCreationByAdminRequestDTO;
import org.scoooting.user.application.dto.request.AdminUpdateUserCommand;
import org.scoooting.user.application.dto.request.CreateUserByAdminCommand;
import org.scoooting.user.application.dto.request.UpdateUserCommand;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.usecase.AddBonusesUseCase;
import org.scoooting.user.application.usecase.user.CreateUserUseCase;
import org.scoooting.user.application.usecase.user.DeleteUserUseCase;
import org.scoooting.user.application.usecase.user.GetUserUseCase;
import org.scoooting.user.application.usecase.user.UpdateUserUseCase;
import org.scoooting.user.application.dto.response.PageResponseDTO;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
@Slf4j
public class UserController {

    private final WebCommandMapper webCommandMapper;

    private final GetUserUseCase getUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final AddBonusesUseCase addBonusesUseCase;

    @Operation(
            summary = "[USER] Get current user profile",
            description = "Available to: ALL authenticated users",
            tags = {"User Operations"}
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("UserController: /me endpoint called");

        if (principal == null) {
            log.error("UserController: Principal is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("UserController: User authenticated - userId: {}, email: {}, role: {}",
                principal.getUserId(), principal.getEmail(), principal.getRole());

        UserResponseDTO user = getUserUseCase.findUserById(principal.getUserId());
        log.info("UserController: Returning user data for userId: {}", principal.getUserId());
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[USER] Update current user profile",
            description = "User can only update their own name and city. Available to: ALL authenticated users",
            tags = {"User Operations"}
    )
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserRequestDTO request
    ) {
        UpdateUserCommand updateUserCommand = webCommandMapper.toUpdateUserCommand(request);
        UserResponseDTO user = updateUserUseCase.updateUser(principal.getUserId(), updateUserCommand);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[STAFF] Get user by email",
            description = "Available to: SUPPORT, ANALYST, ADMIN",
            tags = {"Staff Operations"}
    )
    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ANALYST', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @PathVariable @Email(message = "Invalid email format") String email
    ) {
        UserResponseDTO user = getUserUseCase.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[STAFF] Get user by ID",
            description = "Available to: SUPPORT, ANALYST, ADMIN",
            tags = {"Staff Operations"}
    )
    @GetMapping("/user/{id}")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ANALYST', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable @Min(0) @Max(Long.MAX_VALUE) Long id) {
        UserResponseDTO user = getUserUseCase.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[STAFF] Get all users with pagination",
            description = "Available to: SUPPORT, ANALYST, ADMIN",
            tags = {"Staff Operations"}
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPPORT', 'ANALYST', 'ADMIN')")
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size
    ) {
        PageResponseDTO<UserResponseDTO> users = getUserUseCase.getUsers(email, name, page, size);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.totalElements()))
                .header("X-Total-Pages", String.valueOf(users.totalPages()))
                .body(users);
    }

    @Operation(
            summary = "[ADMIN] Update any user",
            description = "Admin can update all user fields including role. Available to: ADMIN only",
            tags = {"Admin Operations"}
    )
    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminUpdateUser(
            @PathVariable @Min(0) @Max(Long.MAX_VALUE) Long id,
            @Valid @RequestBody AdminUpdateUserRequestDTO request
    ) {
        AdminUpdateUserCommand adminUpdateUserCommand = webCommandMapper.toAdminUpdateUserCommand(request);
        UserResponseDTO user = updateUserUseCase.adminUpdateUser(id, adminUpdateUserCommand);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[ADMIN] Delete user",
            description = "Permanently delete user. Available to: ADMIN only",
            tags = {"Admin Operations"}
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable @Min(0) @Max(Long.MAX_VALUE) Long id) {
        deleteUserUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "[SUPPORT] Add or deduct bonuses",
            description = "Support can add/deduct bonuses for compensation or rewards. Available to: SUPPORT, ADMIN",
            tags = {"Support Operations"}
    )
    @PostMapping("/user/{id}/bonuses")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<UserResponseDTO> addBonuses(
            @PathVariable @Min(0) @Max(Long.MAX_VALUE) Long id,
            @RequestParam Integer amount
    ) {
        UserResponseDTO user = addBonusesUseCase.addBonuses(id, amount);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "[ADMIN] Create user with specific role",
            description = "Create new user with Operator/Support/Analyst role. Available to: ADMIN only",
            tags = {"Admin Operations"}
    )
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUserWithRole(
            @RequestBody @Valid UserCreationByAdminRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("AuthController: Admin {} creating user with email: {} and role: {}",
                principal.getEmail(), request.email(), request.roleName());
        try {
            CreateUserByAdminCommand createUserByAdminCommand = webCommandMapper.toCreateUserByAdminCommand(request);
            AuthResult authResult = createUserUseCase.createUserWithRole(createUserByAdminCommand);
            log.info("AuthController: User created successfully");
            return ResponseEntity.ok(authResult.accessToken());
        } catch (Exception e) {
            log.error("AuthController: Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

}