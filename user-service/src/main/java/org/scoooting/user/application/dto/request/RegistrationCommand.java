package org.scoooting.user.application.dto.request;

public record RegistrationCommand(String email, String name, String password, String cityName) {}
