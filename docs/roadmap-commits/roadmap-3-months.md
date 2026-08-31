# 🗺️ Roadmap de Commits — 3 Meses (RentFlowAI SaaS Engine)

**Plan de Ejecución Commit por Commit (Fase 1 Core & Docker + Fase 2 Spring AI & AWS CLF-C02)**

---

## 1. Visión Estratégica y Leyenda de Estados

Este roadmap detalla la construcción commit por commit de **RentFlowAI** a lo largo de **12 semanas (3 meses)**. Cada commit cuenta con un indicador visual de estado:
- ✅ **Completado:** Trabajo implementado, probado y fusionado en la rama principal.
- 🔴 **Pendiente:** Trabajo planificado para las próximas semanas.

El objetivo de la **Fase 1** es dejar la solución 100% contenerizada localmente con **Docker** y **Docker-Compose**, preparando el terreno para que en la **Fase 2** la infraestructura completa se despliegue en la nube de **AWS (ECS, RDS PostgreSQL, S3, CloudWatch)**, sirviendo como evidencia práctica directa para la obtención de la certificación **AWS Certified Cloud Practitioner (AWS CLF-C02)**.

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                            ROADMAP 3 MESES (12 SEMANAS)                     │
├──────────────────────────┬──────────────────────────┬───────────────────────┤
│  MES 1: CORE & API REST  │  MES 2: PERSISTENCIA &   │  MES 3: CLOUD, CI/CD  │
│  (W1 - W4)               │  FINTECH SECURITY        │  & FASE 2 SPRING AI   │
│  ✅ En Progreso          │  🔴 Planificado          │  🔴 AWS CLF-C02       │
└──────────────────────────┴──────────────────────────┴───────────────────────┘
```

---

## 2. Desglose Detallado por Mes y Semana

### MES 1: Núcleo de Dominio, Aplicación y Capa de Entrada REST

#### Semana 1: Dominio Base (User & Property)
- ✅ **Commit 01:** Configuración inicial del proyecto Java 17 y Spring Boot 3.3.4 con estructura de Arquitectura Hexagonal.
- ✅ **Commit 02:** Modelado del Agregado Raíz `User` (UUID, email, passwordHash, roles).
- ✅ **Commit 03:** Implementación del Enum `Role` (`LANDLORD`, `TENANT`, `ADMIN`).
- ✅ **Commit 04:** Creación del Value Object `Rut` con Regex de validación de formato chileno (12345678-9).
- ✅ **Commit 05:** Métodos de auditoría y constructores en `User` (`createdAt`, `updatedAt`, `updateRut`).
- ✅ **Commit 06:** Modelado del Agregado Raíz `Property` con atributo inmutable `landlordId`.
- ✅ **Commit 07:** Implementación del Enum `PropertyStatus` (`AVAILABLE`, `RENTED`, `MAINTENANCE`).
- ✅ **Commit 08:** Métodos de dominio en `Property` (cambio de canon base, actualización de dirección, asignación de `payoutAccountId`).
- ✅ **Commit 09:** Definición del puerto de salida `UserRepository` (interface pura en `ports/out`).
- ✅ **Commit 10:** Definición del puerto de salida `PropertyRepository` (interface pura en `ports/out`).
- ✅ **Commit 11:** Creación de excepciones semánticas de dominio (`UserNotFoundException`, `PropertyNotFoundException`).
- ✅ **Commit 12:** Suite de pruebas unitarias de dominio con JUnit 5 y `@Nested` para `User` y `Property`.
- ✅ **Commit 13:** Verificación de métricas de calidad en JaCoCo alcanzando 100% de cobertura de líneas y ramas en `user` y `property`.

#### Semana 2: Dominio Financiero, Suscripciones y Motor de Arriendos
- ✅ **Commit 14:** Value Object `Money` (BigDecimal e inmutabilidad con validación de divisas `CLP`, `UF`, `USD`).
- ✅ **Commit 15:** Entidad `Subscription` y enum `PlanType` ajustado a tarifas B2B Chile: `STARTER` (3 prop, 100MB), `PRO` (7 prop, 1GB), `ENTERPRISE` (13 prop, 10GB).
- ✅ **Commit 16:** Modelado de la entidad `BankAccount` para asignación de cuentas de liquidación bancaria.
- ✅ **Commit 17:** Agregado Raíz `RentalContract` (método `calculateLateFee` para multas por mora y `readjustRentByIpc`).
- ✅ **Commit 18:** Agregado Raíz `PaymentRecord` con clave de idempotencia determinista `PAY-{contractId}-{YYYY}-{MM}` y candado `PAID`.
- ✅ **Commit 19:** Definición de los puertos de salida `ContractRepository` y `PaymentRepository`.
- ✅ **Commit 20:** Pruebas unitarias de dominio financiero en TDD alcanzando 100% de cobertura de líneas y ramas en JaCoCo.

#### Semana 3: Orquestación de Capa de Aplicación (Casos de Uso)
- ✅ **Commit 21:** Implementación de `CreatePropertyUseCase` con validación de cuota del plan de suscripción (`subscription.canAddProperty`).
- ✅ **Commit 22:** Implementación de `CreateContractUseCase` con verificación Anti-IDOR del propietario y conmutación atómica a `RENTED`.
- ✅ **Commit 23:** Implementación de `InitiatePaymentCheckoutUseCase` (Checkout Fase 1, congelamiento de mora y filtro `isPending() && dueDate`).
- ✅ **Commit 24:** Implementación de `ProcessPaymentUseCase` (Webhook Fase 2, Anti-Underpayment y liquidación asíncrona).
- ✅ **Commit 25:** Creación de puertos de salida auxiliares (`PaymentGatewayPort`, `NotificationSenderPort`, `EconomicIndicatorPort`).
- ✅ **Commit 26:** Suite de pruebas unitarias de aplicación con Mockito y JUnit 5 (130 tests pasando en verde).

#### Semana 4: Adaptadores de Entrada REST, Mappers DTO y Validaciones (REFINADA)

| Commit | Estado | Módulo / Capa | Componentes y Archivos Incluidos | Propósito & Regla de Negocio |
| :--- | :---: | :--- | :--- | :--- |
| **Commit 27** | ✅ | `infrastructure/rest` | `ContractRequestDTO`, `ContractResponseDTO`, `ContractRestMapper`, `ContractController` (POST `/api/v1/contracts`), `PaymentCheckoutRequestDTO`, `PaymentCheckoutResponseDTO`, `PaymentWebhookRequestDTO`, `PaymentController` (POST `/api/v1/payments/checkout`, POST `/api/v1/payments/webhook`). | Expone los adaptadores de entrada HTTP REST mapeando payloads de transporte hacia comandos de Casos de Uso. |
| **Commit 28** | ✅ | `src/test/java/infrastructure` | `PropertyControllerTest`, `ContractControllerTest`, `PaymentControllerTest`. | Pruebas de integración HTTP de capa de entrada con `MockMvc` verificando códigos de respuesta 200 OK, 201 Created, 400 Bad Request y 403 Forbidden. |
| **Commit 29** | ✅ | `pom.xml` & DTOs | `pom.xml` (`spring-boot-starter-validation`), DTOs (`@NotNull`, `@NotBlank`, `@Positive`, `@Pattern(regexp="^(CLP\|UF\|USD)$")`), Controllers (`@Valid`). | Implementa el filtro de validación declarativa HTTP impidiendo peticiones con montos negativos, monedas no soportadas o UUIDs nulos. |

---

### MES 2: Persistencia JPA, Seguridad Multitenant, Pasarela Real y Cron Jobs

#### Semana 5: Persistencia JPA y Migraciones de BD
- 🔴 **Commit 30:** Configuración de Flyway (`V1__init_schema.sql`) para la creación de tablas `users`, `properties`, `contracts`, `payments`, `subscriptions`, `bank_accounts`.
- 🔴 **Commit 31:** Entidades JPA (`UserEntity`, `PropertyEntity`, `ContractEntity`, `PaymentEntity`, `SubscriptionEntity`) y Mappers JPA bidireccionales.
- 🔴 **Commit 32:** Implementación de Adaptadores de Persistencia (`UserRepositoryAdapter`, `PropertyRepositoryAdapter`, `PaymentRepositoryAdapter`) con Spring Data JPA.

#### Semana 6: Seguridad Enterprise, JWT y Contexto Multitenant
- 🔴 **Commit 33:** Configuración de Spring Security 6 (`SecurityConfig`, Stateless Session, PasswordEncoder BCrypt/Argon2).
- 🔴 **Commit 34:** `JwtAuthenticationFilter`, `JwtTokenProvider` y `CustomUserDetailsService` para autenticación por Roles (`LANDLORD`, `TENANT`, `ADMIN`).
- 🔴 **Commit 35:** `TenantContextFilter` para el aislamiento dinámico de datos multi-tenant por cabecera `X-Tenant-ID`.

#### Semana 7: Integración Pasarela Fintech Real (Stripe & Webpay Plus)
- 🔴 **Commit 36:** Adaptador `StripePaymentGatewayAdapter` implementando `PaymentGatewayPort` con SDK de Stripe / OpenFeign Client.
- 🔴 **Commit 37:** Adaptador `WebpayPaymentGatewayAdapter` para Transbank Webpay Plus (integración bancaria chilena).
- 🔴 **Commit 38:** Manejador de verificación de firma digital (HMAC Secret) en `PaymentController.processWebhook()` para prevenir spoofing de Webhooks.

#### Semana 8: Billing Recurrente, IPC & Cron Jobs
- 🔴 **Commit 39:** `@Scheduled` Job `ReadjustRentByIpcScheduler` para la ejecución automatizada mensual de reajustes por inflación.
- 🔴 **Commit 40:** `@Scheduled` Job `OverduePaymentDetectorScheduler` para la conmutación de pagos impagos a estado `OVERDUE`.
- 🔴 **Commit 41:** Manejo de cobranza recurrente y vencimiento de cuotas de suscripción SaaS (`SubscriptionDunningJob`).

---

### MES 3: Dockerización, Notificaciones, CI/CD Cloud y Fase 2 (Spring Boot AI & AWS CLF-C02)

#### Semana 9: Notificaciones Asíncronas y Comprobantes PDF
- 🔴 **Commit 42:** Adaptador `EmailNotificationAdapter` con `JavaMailSender` y plantillas HTML en Thymeleaf para comprobantes de arriendo.
- 🔴 **Commit 43:** Servicio de generación de recibos digitales en PDF (`PdfReceiptGenerator`) para descarga de inquilinos.
- 🔴 **Commit 44:** Adaptador `S3StorageAdapter` para almacenamiento seguro de archivos en la nube (AWS S3 / LocalStack S3).

#### Semana 10: Pruebas de Integración y Resiliencia Bancaria
- 🔴 **Commit 45:** Configuración de **Testcontainers** con PostgreSQL 16 real para pruebas de integración `@SpringBootTest` sin mocks.
- 🔴 **Commit 46:** Integración de **WireMock** para simular caídas de red, latencia y respuestas 500 de Stripe/Webpay.
- 🔴 **Commit 47:** Implementación de patrones de resiliencia con **Resilience4j** (`@CircuitBreaker`, `@Retry`) en llamadas a pasarelas e APIs externas.

#### Semana 11: Dockerización Completa Local (Fase 1 Ready)
- 🔴 **Commit 48:** Creación de `Dockerfile` multi-stage optimizado (stage 1: build con Maven & OpenJDK 21, stage 2: ejecutable minimal JRE layer).
- 🔴 **Commit 49:** Creación de `docker-compose.yml` integrando PostgreSQL 16, Mailpit (SMTP server), LocalStack (AWS S3 local) y PGAdmin.
- 🔴 **Commit 50:** GitHub Actions CI/CD Pipeline (`.github/workflows/ci-cd.yml`): automatización de build, tests, JaCoCo report y análisis estático con SonarCloud.

#### Semana 12: Fase 2 — Spring Boot AI & Arquitectura AWS Cloud (Certificación AWS CLF-C02)
- 🔴 **Commit 51:** Integración de `spring-ai-starter-openai` / Anthropic Claude en `pom.xml`.
- 🔴 **Commit 52:** Caso de Uso `AuditContractPdfUseCase` implementando `ContractAiExtractorPort` para OCR y análisis estructurado JSON de contratos en PDF con IA.
- 🔴 **Commit 53:** Copiloto Agéntico REVA (RentFlow Virtual Assistant): Pipeline RAG con VectorStore (`pgvector`) para consultas legales e inmobiliarias.
- 🔴 **Commit 54:** Despliegue en producción AWS Cloud (AWS ECS Fargate, AWS RDS PostgreSQL Multi-AZ, AWS S3, CloudWatch) alineado a la preparación de la certificación **AWS Certified Cloud Practitioner (CLF-C02)**.

---

## 3. Matriz de Verificación para Empleabilidad Remota

Al completar el Commit 54, el repositorio de RentFlowAI demostrará a cualquier evaluador técnico internacional:
1. **Calidad de Código Inflexible:** 100% cobertura en dominio, arquitectura desacoplada sin fugas de Spring.
2. **Fintech Readiness:** Idempotencia, manejo de divisas, firmas HMAC, manejo asíncrono de Webhooks.
3. **Enterprise Security:** Anti-IDOR, JWT, RBAC y aislamiento Multitenant.
4. **Cloud & DevOps Capability:** Dockerización local completa en Fase 1 y arquitectura serverless en AWS Cloud en Fase 2 (preparación AWS CLF-C02).
5. **AI Developer Capability:** Aplicación real de LLMs en flujos de producción con Spring AI.