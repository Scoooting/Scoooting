package org.scoooting.user.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.user.dto.request.UserCreationByAdminRequestDTO;
import org.scoooting.user.dto.request.UserRegistrationRequestDTO;
import org.scoooting.user.dto.request.UserSignInDto;
import org.scoooting.user.exceptions.InvalidRefreshTokenException;
import org.scoooting.user.exceptions.UserAlreadyExistsException;
import org.scoooting.user.exceptions.UserNotFoundException;
import org.scoooting.user.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationRequestDTO userRegistrationDto)
            throws UserAlreadyExistsException {
        String token = userService.registerUser(userRegistrationDto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody @Valid UserSignInDto userSignInDto) throws UserNotFoundException {
        String token = userService.signIn(userSignInDto);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null)
            throw new InvalidRefreshTokenException("Invalid refresh token!");

        return ResponseEntity.ok(userService.refreshToken(authHeader.substring(7)));
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
