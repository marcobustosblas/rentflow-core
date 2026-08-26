# Reglas de Negocio de Pagos y Facturación — RentFlowAI

## 1. Introducción y Contexto

En el modelo SaaS de gestión de arriendos **RentFlowAI**, el subdominio de Pagos (`payment`) administra el flujo de dinero entre arrendatarios, propietarios y pasarelas de pago (Stripe, Webpay, etc.). 

Dado que un error en el manejo de transacciones financieras genera pérdidas económicas, desconfianza del usuario y problemas legales, la arquitectura impone reglas de negocio estrictas ("Guardias Absolutos") tanto en la capa de Dominio como en los Casos de Uso de la Capa de Aplicación.

---

## 2. Política Destacada: Prevención de Duplicidad de Facturación (Doble Clic)

### 2.1. Explicación Técnica y Lógica de Filtrado

En `InitiatePaymentCheckoutUseCase`, antes de crear un nuevo registro de intención de pago o solicitar un nuevo link a la pasarela, se ejecuta el siguiente filtro sobre los cobros registrados del contrato:

```java
Optional<PaymentRecord> existingPending = paymentRepository.findByContractId(contractId).stream()
        .filter(p -> p.isPending() && p.getDueDate().equals(dueDate))
        .findFirst();
```

### 2.2. Importancia Vital del `&&` (Y Lógico)

La combinación mediante el operador relacional `&&` (**AND Lógico**) es **indispensable** para la corrección financiera del sistema. Evaluar solo una de las condiciones provocaría fallos graves de negocio:

```mermaid
graph TD
    A[Inicio de Checkout de Pago] --> B{p.isPending() && p.getDueDate().equals(dueDate)}
    B -- Sí (Existe Pendiente) --> C[Reutilizar PaymentRecord existente]
    B -- No (No Existe Pendiente) --> D[Crear nuevo PaymentRecord con Clave PAY-ID-YYYY-MM]
    C --> E[Generar Checkout URL en Pasarela]
    D --> E
```

#### ¿Por qué NO validar ÚNICAMENTE `isPending()`?
- Si la lógica solo validara `p.isPending()`: Si un inquilino tiene una deuda vencida o pendiente de un mes anterior (ej: Enero) e intenta pagar el mes actual (ej: Marzo), el filtro capturaría el pago pendiente de Enero y el sistema intentaría cobrar el mes viejo o asociar la sesión de pago al mes equivocado.
- **Riesgo:** Inconsistencia en la contabilidad mensual y cobro de periodos erróneos.

#### ¿Por qué NO validar ÚNICAMENTE `getDueDate().equals(dueDate)`?
- Si la lógica solo validara `p.getDueDate().equals(dueDate)`: Si el inquilino ya pagó exitosamente la cuota de Marzo (`PaymentStatus.PAID`) y por error vuelve a hacer clic en "Pagar Arriendo" o recarga la URL de la plataforma, el sistema encontraría el registro de Marzo (que está `PAID`), lo tomaría como referencia y generaría una nueva orden de cobro en Webpay.
- **Riesgo:** Cobros duplicados en la tarjeta del inquilino para un mes que ya fue completamente saldado.

#### El Beneficio del `&&` Lógico Combinado:
Garantiza que **solo** si existe un cobro que **todavía está en estado pendiente (`PENDING`)** **Y** cuya **fecha de vencimiento coincide exactamente con el mes en curso (`dueDate`)**, el sistema reutilizará esa orden de pago. Si no cumple ambas condiciones a la vez, se crea una nueva orden controlada o se respeta el estado del pago ya saldado.

---

## 3. Catálogo Completo de Reglas de Negocio Financieras

A continuación se detallan **todas** las reglas de negocio que rigen el motor financiero de RentFlowAI:

### RN-PAY-01: Formato Determinista de Clave de Idempotencia
- **Regla:** Toda intención de pago generada debe poseer una clave única en formato:  
  `PAY-{contractId}-{YYYY}-{MM}` (Ejemplo: `PAY-a1b2c3d4-2026-03`).
- **Propósito:** Previene que la base de datos acepte dos intenciones de pago distintas para la misma propiedad y el mismo periodo mensual.

### RN-PAY-02: Congelamiento de Multas por Mora (Freeze Penalty Rate)
- **Regla:** Al iniciar el checkout (`InitiatePaymentCheckoutUseCase`), el sistema calcula la multa por días de atraso (`calculateTotalWithPenalty`) y la congela dentro del campo `expectedAmount` y `lateFeeApplied` del `PaymentRecord`.
- **Propósito:** Evita fluctuaciones en el monto a pagar mientras el inquilino se encuentra en la pantalla de la pasarela de pago (Webpay/Stripe).

### RN-PAY-03: Escudo Estricto de Monedas (`CurrencyMismatchException`)
- **Regla:** Queda estrictamente prohibido realizar operaciones matemáticas o registrar pagos en monedas distintas a la pactada en el contrato.
- **Ubicación:** Entidad `Money` y método `registerPayment()` en `PaymentRecord`.
- **Excepción:** Si se intenta pagar en `USD` un contrato en `CLP`, el sistema lanza `IllegalArgumentException("Payment currency does not match expected currency")`.

### RN-PAY-04: Piso Mínimo de Pago (Anti-Underpayment)
- **Regla:** Ningún pago puede registrarse como completado si `amountPaid < expectedAmount`.
- **Propósito:** Evita que usuarios maliciosos alteren peticiones HTTP o tokens de respuesta para abonar montos parciales o insignificantes (ej: \$1 peso).

### RN-PAY-05: Inmutabilidad de Pagos Liquidados (`PAID` State Lock)
- **Regla:** Un `PaymentRecord` cuyo estado sea `PAID` no puede ser cancelado (`cancel()`), reembolsado directo sin auditoría, ni re-procesado (`registerPayment()`).
- **Ubicación:** `PaymentRecord.java`.
- **Comportamiento:** Si un Webhook duplicado llega para un pago ya liquidado, el caso de uso `ProcessPaymentUseCase` retorna el objeto intacto sin efectuar escrituras en BD ni enviar notificaciones duplicadas.

### RN-PAY-06: Control de Acceso Multitenant y Protección IDOR
- **Regla:** 
  1. Solo el `Tenant` titular del contrato puede generar su URL de checkout (`contract.getTenantId().equals(tenantId)`).
  2. Solo el `Landlord` dueño verificado de la propiedad puede crear un `RentalContract` sobre ella (`property.getLandlordId().equals(landlordId)`).
- **Propósito:** Garantizar el aislamiento completo de datos entre clientes del SaaS.

### RN-PAY-07: Sincronización del Ciclo de Vida del Inmueble
- **Regla:** La creación y activación de un `RentalContract` cambia de forma atómica el estado de la `Property` de `AVAILABLE` a `RENTED`.
- **Propósito:** Previene que un inmueble arrendado vuelva a ser listado en la oferta pública de la plataforma.

### RN-PAY-08: Validación de Rol de Usuario
- **Regla:** Un contrato solo puede ser asignado a un usuario que tenga otorgado explícitamente el rol `Role.TENANT`.
- **Propósito:** Previene errores de configuración en cuentas de administradores o propietarios.

### RN-PAY-09: Transición Automática a Estado de Mora (`OVERDUE`)
- **Regla:** Si la fecha actual del sistema supera la fecha de vencimiento (`currentDate.isAfter(dueDate)`) y el pago sigue `PENDING`, el método `markAsOverdue()` conmuta el estado a `OVERDUE`.
- **Propósito:** Permite al motor de notificaciones e IA enviar alertas de cobro extrajudicial.

### RN-PAY-10: Auditoría Digital y Emisión Asíncrona de Comprobante
- **Regla:** Todo pago liquidado con éxito debe almacenar la referencia bancaria (`transactionReference`), el enlace del comprobante en PDF (`paymentReceiptUrl`) y notificar al cliente vía `NotificationSenderPort`.
