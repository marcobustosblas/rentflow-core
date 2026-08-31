# Week 4 Development Log: Adapters IN, REST API & Security (17 Commits)

## Objetivo Semanal
Exponer los Casos de Uso del núcleo financiero y de gestión inmobiliaria hacia el exterior mediante una API RESTful, garantizando la validación estricta de datos (Zero Trust), el manejo de webhooks asíncronos y la cobertura de pruebas de la capa HTTP sin levantar la base de datos.

## Implementaciones Técnicas

### 1. Controladores REST (Adapters IN)
* **PropertyController:** Endpoints para la creación y gestión de inmuebles.
* **ContractController:** Endpoints para formalizar contratos de arriendo con reglas financieras (depósitos, multas, días de pago).
* **PaymentController:** Diseño de API en dos fases para pasarelas de pago (Stripe/Webpay):
    * `POST /checkout`: Generación de intención de pago para el inquilino.
    * `POST /webhook`: Recepción asíncrona server-to-server para la liquidación de la transacción.

### 2. DTOs y Mapeo
* Aislamiento total del Dominio: El frontend se comunica exclusivamente a través de Request/Response DTOs.
* Despliegue de conversores manuales (Mappers) para transformar JSONs planos en agregados ricos (ej. ensamblaje del Value Object `Money` a partir de `amount` y `currency`).

### 3. Seguridad y Zero Trust (Jakarta Validation)
* Implementación del escudo `@Valid` en todos los endpoints HTTP.
* Reglas de sanitización en DTOs: `@NotNull`, `@NotBlank`, `@Positive`, y validación de divisas mediante expresiones regulares (`@Pattern(regexp = "^(CLP|UF|USD)$")`).

### 4. Testing de Capa Web (MockMvc)
* Uso de `@WebMvcTest` para aislar la capa de Controladores.
* Simulación de Casos de Uso mediante `@MockBean`.
* Validación de serialización JSON de tipos complejos (`LocalDate`, `BigDecimal`) utilizando `JavaTimeModule` de Jackson.

### 5. Decisiones Arquitectónicas (ADRs)
* **ADR-001 (Bounded Contexts):** Desacoplamiento del cobro de Arriendos vs. Suscripciones SaaS mediante el uso de un `referenceId` agnóstico y el enum `PaymentTarget`.
* **Security & Resilience:** Documentación del Teorema CAP (Prioridad CP) para transacciones financieras y protección contra Parameter Tampering (F12) validando siempre la Fuente de la Verdad en el backend.