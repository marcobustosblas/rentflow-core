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
- Tiene una suscripción activa (`ACTIVE`).

### Flujo Principal (Happy Path)
1. El Landlord envía los datos de la propiedad (dirección, precio base, cuenta bancaria de destino).
2. El sistema recupera la `Subscription` del Landlord.
3. El sistema evalúa `subscription.canAddProperty(currentCount)`.
4. El sistema instancia una nueva `Property` con estado `AVAILABLE`.
5. Si se proporciona un `bankAccountId`, el sistema lo asigna vía `property.assignPayoutAccount()`.
6. El sistema persiste la `Property` a través de `PropertyRepository`.
7. Retorna el UUID de la propiedad creada.

### Flujos Alternativos
- **1a. Límite de Plan Excedido:** Si `canAddProperty` retorna `false`, el sistema lanza `SubscriptionLimitExceededException` (HTTP 403 Forbidden) invitando al usuario a mejorar su plan.
- **1b. Cuenta Bancaria No Encontrada:** Si el `bankAccountId` no pertenece al Landlord, lanza `IllegalArgumentException` (HTTP 400 Bad Request).

---

## UC-02: Generar Contrato de Arriendo (Create Rental Contract)

**Actor Principal:** Landlord
**Componente:** `CreateContractUseCase`

### Precondiciones
- La `Property` existe y su estado es `AVAILABLE`.
- El `Tenant` (inquilino) existe en el sistema.

### Flujo Principal (Happy Path)
1. El Landlord envía los datos (Inquilino, Fecha Inicio/Fin, Día de pago mensual, Multa diaria, Meses reajuste IPC).
2. El sistema valida las reglas de fechas (Fecha Inicio < Fecha Fin).
3. El sistema crea el agregado `RentalContract` con estado `ACTIVE`.
4. El sistema actualiza el estado de la `Property` a `RENTED` (`property.markAsRented()`).
5. Se persisten ambos agregados en una transacción atómica (o usando Eventos de Dominio).
6. Retorna el detalle del contrato.

### Flujos Alternativos
- **2a. Propiedad no disponible:** Si la propiedad está en `MAINTENANCE` o `RENTED`, lanza `IllegalStateException` (HTTP 409 Conflict).

---

## UC-03: Procesar Pago de Arriendo (Process Payment)

**Actor Principal:** Tenant / Sistema Automático Webpay
**Componente:** `ProcessPaymentUseCase`

### Precondiciones
- El `RentalContract` existe y está `ACTIVE`.

### Flujo Principal (Happy Path)
1. El sistema recibe la orden de pago (Monto base, Fecha de pago, `idempotencyKey`).
2. Se consulta a la BD si el `idempotencyKey` ya existe. Si no, continúa.
3. El sistema carga el `RentalContract`.
4. El sistema ejecuta `contract.calculatePaymentDue(paymentDate)`.
    - *Sub-flujo:* Si `paymentDate` > `dueDay`, el dominio suma el `dailyPenalty` al monto base.
5. Se genera un `PaymentRecord` con estado `PAID` y los montos desglosados (Monto Base + Late Fee).
6. El sistema guarda el pago vía `PaymentRepository`.
7. El sistema invoca `NotificationSenderPort` para enviar el comprobante por WhatsApp/Email al Landlord y al Tenant.

### Flujos Alternativos
- **3a. Idempotencia Detectada:** Si el `idempotencyKey` ya existe, el sistema detiene el proceso inmediatamente y devuelve HTTP 200 OK con el recibo del pago anterior (Previene error de doble clic).
- **3b. Pago Insuficiente:** Si el monto transferido es menor al cálculo de `calculatePaymentDue`, se marca el pago como `PARTIAL` (o se rechaza dependiendo de la configuración del Landlord).

---

## UC-04: Reajuste Automático por IPC (Readjust Rent by IPC)

**Actor Principal:** Sistema (Cron Job programado a la 01:00 AM)
**Componente:** `ReadjustRentByIpcUseCase`

### Flujo Principal (Happy Path)
1. El sistema (`Scheduler`) consulta los contratos cuyo ciclo de `ipcReadjustmentMonths` se cumple en el mes actual.
2. Para cada contrato, el sistema llama a `EconomicIndicatorPort` para obtener la variación del IPC.
3. El adaptador `MindicatorAdapter` consulta la API REST de `mindicador.cl` y retorna el % de inflación.
4. El sistema ejecuta `contract.readjustRentByIpc(ipcPercentage)` actualizando el `rentAmount`.
5. Se guardan los contratos actualizados.
6. El sistema invoca `NotificationSenderPort` para notificar al Tenant el nuevo valor del arriendo con 30 días de anticipación.

### Flujos Alternativos
- **4a. Caída de API Externa:** Si `mindicador.cl` no responde, el sistema atrapa la `IndicatorApiException`, alerta al Admin, y reencola la tarea para el día siguiente sin alterar el contrato.

---

## UC-05: Auditoría de Contrato PDF con IA (Audit Contract PDF)

**Actor Principal:** Landlord
**Componente:** `AuditContractPdfUseCase`

### Flujo Principal (Happy Path)
1. El Landlord sube un PDF escaneado de un contrato físico de arriendo.
2. El sistema lee el binario y lo envía al `ContractAiExtractorPort`.
3. El `SpringAiContractExtractorAdapter` envía el documento al modelo LLM (OpenAI/Claude) con un *System Prompt* estricto para extraer salidas estructuradas en JSON.
4. La IA devuelve un DTO `ExtractedContractData` con: nombre del inquilino, monto pactado, día límite de pago y rut.
5. El sistema pre-llena el formulario del FrontEnd con los datos extraídos para que el Landlord solo presione "Confirmar y Crear".

---