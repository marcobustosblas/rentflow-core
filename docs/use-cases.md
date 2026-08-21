# RentFlow Core (RFC) - Casos de Uso y Reglas de Negocio

**Autor:** Marco Orlando Bustos Blas
**Versión:** 1.0
**Arquitectura:** Domain-Driven Design (DDD) / Hexagonal Architecture

Este documento describe los casos de uso principales (Capa de Aplicación) y las invariantes de dominio (Reglas de Negocio) del núcleo financiero de RentFlow.

---

## Invariantes de Dominio (Reglas de Negocio Globales)

Antes de detallar los flujos, estas son las reglas inquebrantables del sistema que viven en la capa `core/domain`:

1. **Inmutabilidad Financiera:** Un registro de pago (`PaymentRecord`) creado con estado `PAID` jamás puede modificar su `amountPaid` ni ser eliminado físicamente (solo Soft Delete en casos extremos autorizados por Admin).
2. **Idempotencia de Pagos:** Todo intento de pago requiere una clave única (`idempotencyKey`). El sistema rechazará transacciones con una clave ya procesada para evitar cobros duplicados.
3. **Restricción SaaS:** Un `Landlord` no puede crear una nueva `Property` si excede el límite de su plan actual (`Subscription.maxProperties`), a menos que haga un *upgrade* de su plan.
4. **Estados de Propiedad:** Un `RentalContract` solo puede asociarse a una `Property` cuyo estado actual sea `AVAILABLE`.

---

## UC-01: Registrar Nueva Propiedad (Create Property)

**Actor Principal:** Landlord
**Componente:** `CreatePropertyUseCase`

### Precondiciones
- El usuario está autenticado y tiene el rol `LANDLORD`.

### Flujo Principal (Happy Path)
1. El Landlord envía los datos de la propiedad (dirección, precio base, cuenta bancaria de destino).
2. El sistema recupera la `Subscription` del Landlord.
3. El sistema cuenta las propiedades actuales del Landlord en la base de datos (`countByLandlordId`).
4. El sistema evalúa `subscription.canAddProperty(currentCount)`.
5. El sistema instancia una nueva `Property` con estado `AVAILABLE`.
6. Si se proporciona un `bankAccountId`, el sistema lo asigna vía `property.assignPayoutAccount()`.
7. El sistema persiste la `Property` a través de `PropertyRepository`.
8. Retorna la propiedad creada.

### Flujos Alternativos
- **1a. Límite de Plan Excedido:** Si `canAddProperty` retorna `false`, el sistema lanza `IllegalStateException` (HTTP 403 Forbidden) invitando al usuario a mejorar su plan.
- **1b. Cuenta Bancaria No Encontrada:** Si el `bankAccountId` no pertenece al Landlord, lanza `IllegalArgumentException` (HTTP 400 Bad Request).

---

## UC-02: Generar Contrato de Arriendo (Create Rental Contract)

**Actor Principal:** Landlord
**Componente:** `CreateContractUseCase`

### Precondiciones
- La `Property` existe y su estado es `AVAILABLE`.
- El `Tenant` (inquilino) existe en el sistema y tiene el rol `TENANT`.

### Flujo Principal (Happy Path)
1. El Landlord envía los datos (Inquilino, Fecha Inicio/Fin, Día de pago mensual, Multa diaria).
2. El sistema busca la `Property` y valida que el `landlordId` que ejecuta la acción sea el dueño real de la propiedad (Prevención IDOR).
3. El sistema valida que la propiedad esté `AVAILABLE`.
4. El sistema busca al `Tenant` y valida que tenga los privilegios correctos.
5. El sistema crea el agregado `RentalContract` con estado `ACTIVE`.
6. El sistema actualiza el estado de la `Property` a `RENTED` (`property.markAsRented()`).
7. Se persisten ambos agregados.
8. Retorna el detalle del contrato.

### Flujos Alternativos
- **2a. Propiedad no disponible:** Si la propiedad está en `MAINTENANCE` o `RENTED`, lanza `IllegalStateException`.
- **2b. Brecha de Seguridad (IDOR):** Si el `landlordId` no coincide con el dueño de la propiedad, el sistema bloquea la operación con `IllegalStateException`.
- **2c. Tenant Inválido:** Si el usuario no existe o no es `TENANT`, se lanza una excepción de negocio.
---

## UC-03: Procesar Pago de Arriendo (Process Payment)

**Actor Principal:** Tenant / Sistema Automático de Pagos (Webhook Webpay)
**Componente:** `ProcessPaymentUseCase`

### Precondiciones
- Si la petición viene del `Tenant`, el usuario debe estar autenticado.
- Si viene de un Webhook, la firma del payload debe ser validada por la Infraestructura.

### Flujo Principal (Happy Path)
1. El sistema recibe la orden de pago (ID del Contrato, Monto pagado, Fecha de pago, `idempotencyKey`).
2. **[Seguridad - Idempotencia]** El sistema consulta al `PaymentRepository` si el `idempotencyKey` ya fue procesado. Si existe, detiene el flujo y retorna éxito inmediatamente para evitar doble cobro.
3. El sistema recupera el `RentalContract` a través del `ContractRepository`.
4. **[Seguridad - Prevención IDOR]** Si el actor es el `Tenant`, el sistema verifica que el `tenantId` del contrato coincida con el usuario autenticado.
5. El sistema evalúa el estado y ejecuta `contract.calculatePaymentDue(paymentDate)`.
   - *Sub-flujo de Dominio:* Si hay atraso, el contrato suma automáticamente la multa diaria al monto base.
6. El sistema crea un `PaymentRecord` mediante su Factory Method seguro, con estado `PAID` y los montos desglosados.
7. El sistema persiste el registro vía `PaymentRepository`.
8. El sistema invoca el puerto `NotificationSenderPort` para enviar el comprobante (Email/WhatsApp) a las partes.
9. Retorna el UUID del registro de pago.

### Flujos Alternativos
- **3a. Pago Insuficiente:** Si el monto transferido es menor al cálculo estricto del Dominio (Arriendo + Multas), el Caso de Uso lanza `IllegalArgumentException` y aborta la transacción.
- **3b. Contrato Inactivo:** Si el contrato está en estado `EXPIRED` o `TERMINATED`, lanza `IllegalStateException`.

---

## UC-04: Reajuste Automático por IPC (Readjust Rent by IPC)

**Actor Principal:** Sistema (Cron Job automatizado)
**Componente:** `ReadjustRentByIpcUseCase`

### Precondiciones
- El Job se ejecuta según la calendarización de la Infraestructura (ej. 01:00 AM del primer día del mes).

### Flujo Principal (Happy Path)
1. El sistema consulta al `ContractRepository` por todos los contratos activos cuyo ciclo de meses para reajuste se cumpla en la fecha actual.
2. Por cada contrato encontrado, el sistema solicita el porcentaje de inflación actual a través del `EconomicIndicatorPort`.
3. El adaptador externo consulta la API oficial (ej. mindicador.cl) y retorna el valor (%).
4. El sistema ejecuta la regla de dominio `contract.readjustRentByIpc(ipcPercentage)`.
5. El sistema guarda la actualización en bloque (batch save) mediante el `ContractRepository`.
6. El sistema invoca `NotificationSenderPort` para alertar al Inquilino sobre el nuevo valor del canon de arriendo.

### Flujos Alternativos
- **4a. Falla de API Externa:** Si el servicio de indicadores económicos arroja timeout, el Caso de Uso atrapa la excepción, registra el error y detiene el proceso de ese contrato específico para reintentarlo al día siguiente, protegiendo la integridad de los datos.

---

## UC-05: Auditoría y Extracción de Contrato PDF con IA (Audit Contract PDF)

**Actor Principal:** Landlord
**Componente:** `AuditContractPdfUseCase`

### Precondiciones
- El Landlord está autenticado.

### Flujo Principal (Happy Path)
1. El Landlord envía un archivo binario (PDF/Imagen) del contrato impreso a la API.
2. El sistema recibe el flujo de bytes y lo delega inmediatamente al `ContractAiExtractorPort`.
3. La capa de infraestructura (Spring AI / LLM) procesa el documento utilizando un System Prompt estricto.
4. El puerto retorna un DTO (Data Transfer Object) estructurado con: nombre, montos, fechas, multas y banderas de riesgo legal (Semáforo).
5. El Caso de Uso retorna este DTO hacia el adaptador de entrada (Controller) para pre-llenar la interfaz visual del usuario.
   *Nota: Este Caso de Uso no altera el estado de la base de datos de RentFlow, es puro de lectura y extracción asistida.*

---