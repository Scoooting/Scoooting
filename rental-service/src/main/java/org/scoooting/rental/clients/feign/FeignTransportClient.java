package org.scoooting.rental.clients.feign;

import org.scoooting.rental.dto.UpdateCoordinatesDTO;
import org.scoooting.rental.dto.response.TransportResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "transport-service", url = "${transport-service.url:}", path = "/api/transports")
public interface FeignTransportClient {

    @GetMapping("/status/{name}")
    ResponseEntity<Long> getTransportStatusId(@PathVariable String name);

    @GetMapping("/{id}")
    ResponseEntity<TransportResponseDTO> getTransport(@PathVariable Long id);

    @PutMapping("/{id}/status")
    ResponseEntity<TransportResponseDTO> updateTransportStatus(
            @PathVariable Long id,
            @RequestParam String status
    );

    @PutMapping("/update-coordinates")
    ResponseEntity<Void> updateTransportCoordinates(@RequestBody UpdateCoordinatesDTO updateCoordinatesDTO);
}
