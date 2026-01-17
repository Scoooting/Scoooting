package org.scoooting.user.application.dto.request;

public record AdminUpdateUserCommand(String name, String email, String cityName, Integer bonuses,
                                     String roleName  // Может менять роль
) {}