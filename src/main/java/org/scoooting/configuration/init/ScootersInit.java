package org.scoooting.configuration.init;

import lombok.RequiredArgsConstructor;
import org.scoooting.entities.City;
import org.scoooting.entities.Scooter;
import org.scoooting.repositories.CityRepository;
import org.scoooting.repositories.ScooterRepository;
import org.scoooting.utils.ScooterAPI;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScootersInit implements ApplicationRunner {

    private final ScooterRepository scooterRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Scooter> foundScooters = new ScooterAPI().findNearestScooters();
        scooterRepository.saveAll(foundScooters);
    }

}
