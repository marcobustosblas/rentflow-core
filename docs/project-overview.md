# 🚀 Visión General del Proyecto: RentFlowAI

**Documento Master de Propósito, Alcance, Monetización B2B, Dockerización y Estrategia Cloud (AWS CLF-C02)**

---

## 1. Visión Ejecutiva y Propósito Fundamental

**RentFlowAI** es una plataforma **Enterprise SaaS Multi-tenant** diseñada para transformar de extremo a extremo la gestión de activos inmobiliarios, la recaudación automatizada de arriendos, la reconciliación bancaria en tiempo real y la auditoría asistida por Inteligencia Artificial.

En el mercado latinoamericano e internacional (con foco inicial en Chile y expansión B2B), la administración de propiedades sufre de altos índices de morosidad, cobros duplicados, fricción en la reconciliación de transferencias bancarias y falta de trazabilidad legal en el reajuste de cánones por inflación (IPC). **RentFlowAI** resuelve estos dolores críticos mediante un motor financiero determinista blindado con **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal**, ofreciendo una solución robusta de grado bancario.

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                              RENTFLOW AI SaaS                               │
├───────────────────────────────────┬─────────────────────────────────────────┤
│    FASE 1: CORE & DOCKERIZACIÓN   │    FASE 2: SPRING AI & AWS CLOUD        │
│  - DDD + Arquitectura Hexagonal   │  - Spring Boot AI & LLM Integration     │
│  - Motor de Pagos & Idempotencia  │  - Extracción OCR / PDF de Contratos    │
│  - Docker & Docker-Compose Local  │  - Despliegue en AWS (ECS/RDS/S3)       │
│  - Reajuste Automático por IPC    │  - Alineado a Certificación AWS CLF-C02 │
└───────────────────────────────────┴─────────────────────────────────────────┘
```

---

## 2. Alcance Detallado del Proyecto: Fase 1 vs. Fase 2

### 2.1 Fase 1: Core SaaS, Motor Financiero y Dockerización Completa (Etapa Actual)
La Fase 1 establece los cimientos de software de grado profesional, asegurando que toda la aplicación funcione de forma autónoma, desacoplada y reproducible en cualquier entorno mediante contenedores **Docker**:

- **Modelado de Agregados DDD:** Entidades inmutables (`User`, `Property`, `RentalContract`, `PaymentRecord`, `Subscription`, `BankAccount`) con invariantes de dominio infranqueables.
- **Capa de Aplicación & Casos de Uso:** Orquestación pura (`CreatePropertyUseCase`, `CreateContractUseCase`, `InitiatePaymentCheckoutUseCase`, `ProcessPaymentUseCase`, `ReadjustRentByIpcUseCase`).
- **Infraestructura REST & DTOs:** Adaptadores de entrada REST con Spring Boot 3.3.4, validaciones declarativas con `spring-boot-starter-validation` (`@NotNull`, `@NotBlank`, `@Positive`, `@Pattern`), Mappers manuales y manejo global de excepciones (`@ControllerAdvice`).
- **Persistencia JPA & PostgreSQL:** Esquema relacional optimizado con Spring Data JPA y migraciones de BD gestionadas por Flyway.
- **Seguridad Multitenant Enterprise:** Autenticación Stateless con JWT, Spring Security 6, Role-Based Access Control (RBAC: `LANDLORD`, `TENANT`, `ADMIN`) y contexto multitenant (`TenantContextFilter`).
- **Dockerización Local Exhaustiva:**  
  Empaquetamiento completo mediante `Dockerfile` multi-stage (build con JDK 21 y ejecutable minimal JRE) y entorno multinodo con `docker-compose.yml` que orquesta:
  - **PostgreSQL 16:** Base de datos principal.
  - **Mailpit:** Servidor SMTP local para captura y auditoría de emails de prueba.
  - **LocalStack S3:** Emulación local de AWS S3 para almacenamiento de contratos y recibos PDF.
  - **PGAdmin:** Consola de administración de base de datos.

### 2.2 Fase 2: RentFlowAI Copilot, Inteligencia Artificial y Despliegue en AWS Cloud
La Fase 2 eleva el producto a la categoría de SaaS Inteligente impulsado por IA y desplegado en la nube de Amazon Web Services (AWS):

- **Spring Boot AI Integration:** Conexión nativa con modelos de lenguaje masivos (OpenAI GPT-4o / Anthropic Claude 3.5) vía Spring AI.
- **Auditoría de Contratos PDF (`AuditContractPdfUseCase`):** Procesamiento OCR y extracción JSON estructurada de cláusulas financieras, fechas de reajuste y alertas de riesgo legal.
- **Copiloto Agéntico REVA (RentFlow Virtual Assistant):** Pipeline RAG (Retrieval-Augmented Generation) integrado con `pgvector` para responder consultas sobre normativas de arriendo (Ley de Arrendamiento 18.101 en Chile) y contratos.
- **Despliegue Enterprise en AWS Cloud:**
  - **AWS ECS (Elastic Container Service / Fargate):** Orquestación de contenedores Docker serverless.
  - **AWS RDS PostgreSQL:** Base de datos relacional administrada con alta disponibilidad (Multi-AZ).
  - **AWS S3 (Simple Storage Service):** Almacenamiento seguro de documentos PDF y recibos digitales.
  - **AWS CloudWatch & Route53:** Monitoreo de logs, métricas, alertas en tiempo real y DNS de dominio oficial.
- **Alineación con Certificación AWS:** Este proyecto está diseñado paso a paso para servir como evidencia práctica directa para obtener la certificación **AWS Certified Cloud Practitioner (CLF-C02)**.

---

## 3. Planes de Suscripción y Modelo de Monetización B2B (Mercado Chileno CLP)

El modelo de negocios de **RentFlowAI** está adaptado a las necesidades de pequeños y medianos inversionistas inmobiliarios y administradoras de propiedades en Chile, ofreciendo cobros en Pesos Chilenos (CLP) con un costo optimizado por propiedad (~$9.200 - $9.500 CLP/mes) y opción de **descuento por pago anual (2 meses gratis)**:

| Plan | Canon Mensual | Canon Anual (2 meses gratis) | Límite Propiedades | Almacenamiento PDF | Funcionalidades Incluidas |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **STARTER** | **$27.900 CLP / mes** | $279.000 CLP / año | Hasta 3 propiedades | 100 MB | Recaudación básica, reajuste IPC automatizado, recordatorios automáticos por WhatsApp. |
| **PRO** | **$64.900 CLP / mes** | $649.000 CLP / año | Hasta 7 propiedades | 1.000 MB (1 GB) | Todo Starter + Lectura OCR de comprobantes con IA, Webhooks automáticos. |
| **ENTERPRISE** | **$119.900 CLP / mes** | $1.199.000 CLP / año | Hasta 13 propiedades | 10.000 MB (10 GB) | Todo Pro + Auditoría de contratos PDF con Spring AI, Multi-cuenta bancaria de liquidación, soporte prioritario. |

---

## 4. Objetivos Profesionales y Estrategia de Empleabilidad Remota

Este proyecto es una pieza clave de marca personal de ingeniería para conseguir trabajo **100% Remoto** en empresas tecnológicas internacionales (EE.UU., Europa y Latinoamérica):

1. **Demostración de Arquitectura Limpia:** DDD y Hexagonal aplicados sin acoplar el core a Spring Boot ni librerías de terceros.
2. **Fintech Excellence:** Idempotencia determinista (`PAY-{contractId}-{YYYY}-{MM}`), manejo de divisas (`CLP`, `UF`, `USD`), validaciones anti-underpayment y firmas HMAC en Webhooks.
3. **Calidad Inflexible:** TDD con 100% de cobertura funcional y de ramas en JaCoCo.
4. **Cloud & DevOps Readiness:** Dominio de Docker de principio a fin (Fase 1) y AWS Cloud Architecture (Fase 2).
5. **Certificación Oficial:** Soporte práctico directo para obtener la certificación **AWS Certified Cloud Practitioner (CLF-C02)**.
