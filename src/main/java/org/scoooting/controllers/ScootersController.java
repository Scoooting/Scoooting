package org.scoooting.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.ScootersDTO;
import org.scoooting.services.ScooterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scooters")
public class ScootersController {

    private final ScooterService scooterService;

    /**
     * Returns all scooters in 2km range
     * @param lat - user's latitude
     * @param lon - user's longitude
     */
    @GetMapping("/nearestScooters")
    public ResponseEntity<List<ScootersDTO>> findNearestScooters(@RequestParam float lat, @RequestParam float lon) {
        return ResponseEntity.ok(scooterService.findNearestScooters(lat, lon));
    }

    /**
     * Returns scooters in specified city
     */
    @GetMapping("/scootersInCity")
    public ResponseEntity<List<ScootersDTO>> findScootersInCity(@RequestParam String city,
                                                                @RequestParam int offset,
                                                                @RequestParam int limit) {
        return ResponseEntity.ok(scooterService.findScootersInCity(city, offset, limit));
    }
}
