package org.scoooting.rental.adapters.message.feign;

import org.scoooting.rental.application.dto.TransportResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "transport-service", url = "${transport-service.url:}", path = "/api/transports")
public interface FeignTransportClient {

    @GetMapping("/status/{name}")
    ResponseEntity<Long> getTransportStatusId(@PathVariable String name);

    @GetMapping("/{id}")
    ResponseEntity<TransportResponseDTO> getTransport(@PathVariable Long id);
}