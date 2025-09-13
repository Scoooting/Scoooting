package org.scoooting.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.ScootersDto;
import org.scoooting.services.ScooterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/scooters")
public class ScootersController {

    private final ScooterService scooterService;

    @GetMapping("/nearestScooters")
    public ResponseEntity<List<ScootersDto>> findNearestScooters(@RequestParam float lat, @RequestParam float lon) {
        return ResponseEntity.ok(scooterService.findNearestScooters(lat, lon));
    }

    @GetMapping("/scootersInCity")
    public ResponseEntity<List<ScootersDto>> findScootersInCity(@RequestParam String city,
                                                                @RequestParam int offset,
                                                                @RequestParam int limit) {
        return ResponseEntity.ok(scooterService.findScootersInCity(city, offset, limit));
    }
}
