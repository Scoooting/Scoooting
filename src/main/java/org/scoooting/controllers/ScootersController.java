package org.scoooting.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.services.ScooterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScootersController {

    private final ScooterService scooterService;

    @GetMapping("/scooters")
    public ResponseEntity<Double[]> findNearestScootersInDistrict() {
        return ResponseEntity.ok(scooterService.findNearestScootersInDistrict("d"));
    }
}
