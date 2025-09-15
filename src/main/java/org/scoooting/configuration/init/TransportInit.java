package org.scoooting.configuration.init;

import lombok.RequiredArgsConstructor;
import org.scoooting.entities.Transport;
import org.scoooting.repositories.TransportRepository;
import org.scoooting.utils.TransportAPI;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TransportInit implements ApplicationRunner {

    private final TransportRepository transportRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Transport> transports = new TransportAPI().generateInitialTransports();
        transportRepository.saveAll(transports);
    }
}

