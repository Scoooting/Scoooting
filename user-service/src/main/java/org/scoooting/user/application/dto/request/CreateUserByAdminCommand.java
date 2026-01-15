package org.scoooting.user.application.dto.request;

public record CreateUserByAdminCommand(String email, String name, String password, String roleName, String cityName) { }
