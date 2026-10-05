package com.marco.rentflow.infrastructure.adapters.out.event;

import com.marco.rentflow.core.domain.DomainEvent;
import com.marco.rentflow.core.domain.common.ports.out.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador que delega la publicación de eventos al bus de eventos de Spring.
 * Gracias a esto, la capa de Aplicación se mantiene limpia y sin dependencias de Spring.
 */

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher delegate;

    public SpringDomainEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publish(DomainEvent event) {
        delegate.publishEvent(event);
    }

}
