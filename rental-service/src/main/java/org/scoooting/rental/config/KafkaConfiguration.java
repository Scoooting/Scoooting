package org.scoooting.rental.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {

    @Bean
    public NewTopic createRentalStatsTopic() {
        return new NewTopic("reports-data", 1, (short) 1);
    }
}
