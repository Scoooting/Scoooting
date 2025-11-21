package org.scoooting.rental.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {

    @Bean
    public NewTopic createRentalStatsTopic() {
        return new NewTopic("rentalStats", 1, (short) 3);
    }
}
