package org.scoooting.files;

import org.springframework.boot.SpringApplication;

public class TestFileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(FileServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
