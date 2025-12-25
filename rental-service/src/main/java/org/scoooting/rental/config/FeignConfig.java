package org.scoooting.rental.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignConfig {

    @Bean
    public HttpMessageConverters messageConverters() {
        return new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}