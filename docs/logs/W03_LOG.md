# Bitácora de Desarrollo — Semana 3: Orquestación de Aplicación y Motor de Pagos (DDD + Clean Architecture)

## Resumen Ejecutivo

Durante la **Semana 3**, elevamos **RentFlowAI** al siguiente nivel arquitectónico conectando las entidades del Dominio Financiero desarrolladas en la Semana 2 mediante la **Capa de Aplicación (`application/usecase`)**.

Se implementó el ciclo completo de procesamiento de pagos en dos fases asíncronas (Fase 1: Checkout, Fase 2: Confirmación por Webhook), garantizando protección total contra vulnerabilidades de seguridad (IDOR), condiciones de carrera y cobros duplicados mediante claves de idempotencia deterministas.

La suite de pruebas unitarias fue expandida con **Mockito** y **JUnit 5**, 
alcanzando **130 pruebas unitarias en verde** y un **100% de cobertura funcional y de ramas**.

---

## Hitos Logrados y Decisiones de Ingeniería

### 1. Orquestación del Flujo de Pagos en Dos Fases
- **Fase 1: Intención de Pago (Checkout):**  
  Implementada en `InitiatePaymentCheckoutUseCase`. Valida la propiedad del contrato, 
  calcula multas/reajustes, congela el monto esperado y genera la URL de checkout hacia la pasarela (Webpay/Stripe).
- **Fase 2: Confirmación y Liquidación (Webhook):**  
  Implementada en `ProcessPaymentUseCase`. Recibe la notificación asíncrona de la pasarela, 
  valida la clave de idempotencia, verifica la suficiencia del monto abonado, conmuta el estado 
  a `PAID` y emite el comprobante por correo electrónico.

### 2. Blindaje Financiero y Prevención de Duplicidad (Doble Clic)
- Se implementó la regla de filtrado determinista `p.isPending() && p.getDueDate().equals(dueDate)`.
- Evita que múltiples clics del inquilino o recargas de navegador generen cobros duplicados para el mismo periodo mensual o arrastren deudas de meses anteriores.

### 3. Protección Anti-IDOR (Insecure Direct Object Reference)
- Validación estricta de pertenencia de entidades en todos los casos de uso:
  - `CreateContractUseCase`: Valida `property.getLandlordId().equals(landlordId)`.
  - `InitiatePaymentCheckoutUseCase`: Valida `contract.getTenantId().equals(tenantId)`.

### 4. Suite de Pruebas Unitarias de Grado Bancario (Mockito)
- Creación y refactorización de suites de pruebas con clases anidadas (`@Nested`) y nombres descriptivos (`@DisplayName`).
- Cobertura completa del "Happy Path" y escenarios de borde (tokens fantasma, montos insuficientes, discrepancias de divisa USD/CLP, reintentos de red de pasarelas).

---

## Casos de Uso Desarrollados en la Semana 3

| Caso de Uso | Paquete | Descripción / Responsabilidad Principal |
| :--- | :--- | :--- |
| `CreateContractUseCase` | `com.marco.rentflow.core.application.usecase.contract` | Orquesta la creación de contratos de arriendo, valida disponibilidad y conmuta la propiedad a `RENTED`. |
| `InitiatePaymentCheckoutUseCase` | `com.marco.rentflow.core.application.usecase.payment` | Genera intenciones de pago, previene cobros duplicados y entrega la URL segura del Gateway. |
| `ProcessPaymentUseCase` | `com.marco.rentflow.core.application.usecase.payment` | Procesa Webhooks de pasarelas de pago, liquida la deuda y emite notificaciones. |

---

## Estándar Sugerido para Bitácoras Futuras (W04, W05, W06...)

Para mantener la excelencia documental en las próximas semanas del proyecto, se establece la siguiente estructura estandarizada para todos los archivos `WXX_LOG.md`:

```markdown
# Bitácora de Desarrollo — Semana X: [Título del Módulo]

## Resumen Ejecutivo
[Breve síntesis de los logros, arquitectura implementada y valor entregado al negocio]

## Hitos Logrados
1. [Hito 1: Nombre y descripción técnica]
2. [Hito 2: Nombre y descripción de arquitectura/seguridad]
3. [Hito 3: Métricas de Calidad y Pruebas]

## Componentes y Arquitectura
- [Listado de clases, adaptadores o puertos creados]

## Métricas de Calidad y Cobertura (JaCoCo / JUnit)
- Pruebas Unitarias: XX tests pasando en verde.
- Cobertura de Código: XX% en líneas y ramas.

## Próximos Pasos (Semana X+1)
- [Punto 1]
- [Punto 2]
```

---

## Próximos Pasos (Semana 4: Capa de Infraestructura)

- Implementar los Adaptadores de Persistencia con **Spring Data JPA** y **PostgreSQL**.
- Configurar Controllers REST y Handlers de Webhooks para Stripe y Webpay.
- Integración preliminar de **Spring AI** para lectura inteligente de contratos en PDF (OCR).