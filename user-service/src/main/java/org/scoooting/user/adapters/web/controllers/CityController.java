package org.scoooting.user.adapters.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.usecase.GetCityUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final GetCityUseCase getCityUseCase;

    @GetMapping("/city/{id}")
    public ResponseEntity<String> getCityById(@PathVariable("id") @Valid Long id) {
        return ResponseEntity.ok(getCityUseCase.getCityById(id));
    }
}
