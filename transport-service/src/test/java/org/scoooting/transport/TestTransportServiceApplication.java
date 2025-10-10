package org.scoooting.transport;

import org.springframework.boot.SpringApplication;

public class TestTransportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(TransportServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
