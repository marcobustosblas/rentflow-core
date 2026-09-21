# RentFlowAI - Log Semana 7 (W07)
## Ecosistema de Pagos, Webhooks y Manejo de Excepciones

### 🎯 Objetivos Alcanzados

- **Dominio Agnóstico (Bounded Context):** Refactorización profunda de `PaymentRecord` para desacoplarlo 
de las entidades de Arriendo. Ahora utiliza un `referenceId` polimórfico y el enum `PaymentTarget` 
(`RENT`, `SAAS`), permitiendo que el motor financiero procese cualquier tipo de cobro futuro sin 
modificar su núcleo.
- **Mappers y Persistencia:** Creación e integración de `BankAccountPersistenceMapper`, 
`PaymentPersistenceMapper` y `SubscriptionPersistenceMapper`. Se unificó el manejo de fechas a 
`LocalDateTime` en las tres capas (Dominio, JPA y SQL) para garantizar precisión en los ciclos de facturación.
- **Asincronía Transaccional:** Implementación del patrón `PaymentWebhookDispatcher`. 
Este orquestador recibe los eventos asíncronos de pasarelas de pago (Stripe/Webpay) y enruta la 
transacción dinámicamente según el objetivo del pago, protegiendo el `ProcessPaymentUseCase` de la lógica externa.
- **Capa Web (Driving Adapters):** Exposición de los endpoints en `PaymentController` 
(`/checkout` y `/webhook`) utilizando Java Records como DTOs inmutables para la comunicación
con el frontend.
- **Escudo de Excepciones:** Desarrollo del `GlobalExceptionHandler` utilizando 
`@RestControllerAdvice`. Las reglas de negocio (ej. `IllegalArgumentException`) y las excepciones 
personalizadas (`ContractNotFoundException`) son interceptadas y transformadas automáticamente en
respuestas HTTP limpias (400, 404, 409), previniendo fugas de *stacktraces* y permitiendo 
rollbacks automáticos en la base de datos.

### 💡 Decisiones Arquitectónicas Clave

1. **Zero Trust en el Dominio:** Los Value Objects (como `Money`) y Agregados (como `PaymentRecord` y `Subscription`) validan su propia integridad matemática y cronológica antes de permitir cambios de estado.
2. **Fechas Explícitas en SaaS:** Evolución del modelo de `Subscription` para rastrear explícitamente `currentPeriodStart` y `currentPeriodEnd`, facilitando la auditoría de facturación B2B para perfiles Admin.
3. **Filtros Dinámicos:** Reemplazo de métodos estáticos en repositorios por filtros dinámicos mediante Query Parameters (`@RequestParam`) en controladores, delegando la búsqueda específica al motor de base de datos.

### ⏭️ Próximos Pasos (W08)
- Endpoints de gestión de Usuarios (Inquilinos y Propietarios).
- Endpoints de Suscripciones SaaS (Asignación y Cancelación).
- Configuración de auditoría de código con JaCoCo al 85% de cobertura mínima.