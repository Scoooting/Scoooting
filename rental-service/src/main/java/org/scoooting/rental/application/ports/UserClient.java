package org.scoooting.rental.application.ports;

import org.scoooting.rental.application.dto.UserResponseDTO;

public interface UserClient {

    UserResponseDTO getUserById(Long id);

    UserResponseDTO addBonuses(Long id, Integer amount);

    Long getIdByCity(String name);

}
