package org.scoooting.transport.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class FlywayConfig {
//
//    @Value("${spring.datasource.url}")
//    private String datasourceUrl;
//
//    @Value("${spring.datasource.username}")
//    private String datasourceUsername;
//
//    @Value("${spring.datasource.password}")
//    private String datasourcePassword;
//
//    @Bean(initMethod = "migrate")
//    public Flyway flyway() {
//        return Flyway.configure()
//                .dataSource(datasourceUrl, datasourceUsername, datasourcePassword)
//                .locations("classpath:db/migration")
//                .baselineOnMigrate(true)
//                .load();
//    }
}