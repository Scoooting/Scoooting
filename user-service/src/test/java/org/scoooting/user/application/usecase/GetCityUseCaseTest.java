package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.domain.exceptions.DataNotFoundException;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.repositories.CityRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCityUseCaseTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private GetCityUseCase getCityUseCase;

    @Test
    void getCityById_Success() {
        // Arrange
        City city = new City(1L, "SPB");
        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        // Act
        String result = getCityUseCase.getCityById(1L);

        // Assert
        assertEquals("SPB", result);
    }

    @Test
    void getCityById_NotFound_ThrowsException() {
        // Arrange
        when(cityRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class,
                () -> getCityUseCase.getCityById(999L));
    }
}