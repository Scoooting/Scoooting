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
@Deprecated
public class TransportInit implements ApplicationRunner {

    private final TransportRepository transportRepository;

    @Override
    public void run(ApplicationArguments args) {
        /*
        * Note to myself:
        * This place is very handy for adding big chunks of data,
        * which would be helpful to showcase a system properly.
        * However, we don't use it currently to simplify our system.
        * TODO: can be used later
        *
        * */
        // List<Transport> transports = new TransportAPI().generateInitialTransports();
        // transportRepository.saveAll(transports);
    }
}

