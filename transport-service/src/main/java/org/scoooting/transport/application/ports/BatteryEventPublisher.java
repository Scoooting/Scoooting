package org.scoooting.transport.application.ports;

import reactor.core.publisher.Mono;

public interface TransportEventPublisher {

    Mono<Void> publish()
}
