package org.scoooting.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.scoooting.dto.request.LoginRequestDTO;
import org.scoooting.dto.response.AuthResponseDTO;
import org.scoooting.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
