package org.scoooting.user.adapters.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.user.adapters.web.mappers.WebCommandMapperDto;
import org.scoooting.user.adapters.web.request.UserRegistrationDTO;
import org.scoooting.user.adapters.web.request.UserSignInDto;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.dto.request.RegistrationCommand;
import org.scoooting.user.application.dto.request.SignInCommand;
import org.scoooting.user.application.usecase.RefreshTokenUseCase;
import org.scoooting.user.application.usecase.user.UserAuthUseCase;
import org.scoooting.user.domain.exceptions.InvalidRefreshTokenException;
import org.scoooting.user.domain.exceptions.UserAlreadyExistsException;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final WebCommandMapperDto webCommandMapperDto;
    private final UserAuthUseCase userAuthUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationDTO userRegistrationDto)
            throws UserAlreadyExistsException {
        RegistrationCommand registrationCommand = webCommandMapperDto.toRegistrationCommand(userRegistrationDto);
        AuthResult authResult = userAuthUseCase.registerUser(registrationCommand);
        return ResponseEntity.ok(authResult.accessToken());
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody @Valid UserSignInDto userSignInDto) throws UserNotFoundException {
        SignInCommand signInCommand = webCommandMapperDto.toSignInCommand(userSignInDto);
        AuthResult authResult = userAuthUseCase.signIn(signInCommand);
        return ResponseEntity.ok(authResult.accessToken());
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null)
            throw new InvalidRefreshTokenException("Invalid refresh token!");

        return ResponseEntity.ok(refreshTokenUseCase.refresh(authHeader.substring(7)).accessToken());
    }

}
