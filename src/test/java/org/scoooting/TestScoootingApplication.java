package org.scoooting;

import org.springframework.boot.SpringApplication;

public class TestScoootingApplication {

    public static void main(String[] args) {
        SpringApplication.from(ScoootingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
