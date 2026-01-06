package org.scoooting.transport.application.ports.clients;

import reactor.core.publisher.Mono;

public interface UserClient {

    Mono<String> getCityName(Long cityId);


}
