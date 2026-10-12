package com.marco.rentflow.core.domain.common.ports.out;

import com.marco.rentflow.core.domain.DomainEvent;

// Es el "Megáfono". El dominio usa esto para gritar a los cuatro vientos que algo pasó,
// sin importarle quién lo escuche.

/**
 * Port for publishing domain events.
 *
 * La capa de dominio no sabe CÓMO se envían los eventos; solo anuncia hechos.
 * La capa de Infraestructura es la que decide si los entrega en memoria
 * o usa un sistema avanzado de colas (como RabbitMQ o Kafka).
 */

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
