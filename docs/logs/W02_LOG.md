# Bitácora de Desarrollo - Semana 2: Lógica Financiera y Motor de Arriendos

## Resumen Ejecutivo
Durante la Semana 2, nos enfocamos en construir el **Corazón Financiero** del sistema dentro de la capa de Dominio. El objetivo principal fue modelar los acuerdos legales (`RentalContract`), el flujo mensual de caja (`PaymentRecord`) y la gestión del modelo SaaS (`Subscription`).
Se garantizó la precisión matemática absoluta utilizando `BigDecimal` (a través del Value Object `Money`) y se alcanzó un **100% de cobertura de código (Líneas y Ramas)** con JaCoCo, asegurando que las reglas de negocio sean impenetrables.

---

## Agregados y Reglas de Negocio Implementadas

### 1. El Agregado Raíz: `RentalContract`
Es el contrato legal y financiero entre el Propietario y el Inquilino.
* **Cálculo de Multas:** Método dinámico `calculateLateFee` que evalúa los días de atraso y aplica la tasa diaria exacta.
* **Reajuste por IPC:** Lógica encapsulada (`readjustRentByIpc`) para actualizar el canon de arriendo respetando ventanas de tiempo estrictas (ej. no antes de 6 meses) y validando fechas dentro de los límites del contrato.
* **Transiciones de Estado:** Flujos controlados para cambiar el contrato entre `ACTIVE`, `EXPIRED` y `TERMINATED`.

### 2. El Agregado Raíz: `PaymentRecord`
Representa la cuota mensual generada y su ciclo de vida.
* **Idempotencia (`idempotencyKey`):** Implementación de una clave única generada al crear el cobro para evitar la duplicidad de registros si el usuario hace "doble clic" o si un webhook bancario se dispara dos veces.
* **Protección contra Fraudes:** Reglas de estado inmutables que impiden cancelar (`cancel()`) o modificar un pago que ya ha sido liquidado (`PAID`).
* **Control de Pagos Insuficientes:** Validación que rechaza cualquier abono que sea menor a la suma del arriendo más su multa por mora correspondiente.

### 3. Value Object: `Money`
* Encapsulamiento del valor (`BigDecimal`) y la moneda (`Currency` como CLP, USD).
* Impide la creación de montos negativos.
* Protege al sistema lanzando una excepción `CurrencyMismatchException` si se intenta sumar o restar montos de monedas distintas.

### 4. Entidad SaaS: `Subscription`
* Controla los límites de la plataforma según el plan adquirido (`STARTED`, `PRO`, `ENTERPRISE`).
* Determina dinámicamente la capacidad máxima de propiedades y almacenamiento (MB) permitidos para el propietario.
* Manejo automático de los ciclos de facturación (mensual o anual) calculando la fecha exacta de expiración.

---

## Arquitectura Hexagonal: Puertos de Salida (Ports OUT)

Se definieron las interfaces puras (`ContractRepository` y `PaymentRepository`) en el subpaquete `ports/out`. Estas interfaces dictan exactamente qué operaciones de persistencia necesita el dominio (guardar, buscar por ID, buscar por Idempotency Key) sin acoplar el núcleo a Spring Data JPA o PostgreSQL.

---

## Métricas de Calidad y Cobertura (JaCoCo)
* **Test-Driven Development (TDD):** Implementación de pruebas unitarias comprensivas simulando escenarios de inquilinos "ejemplares", "atrasados" y "tramposos".
* **Data-Driven Tests:** Uso de `@ParameterizedTest` y `@CsvSource` para validar el cálculo exacto de multas a lo largo de distintos días de mora.
* **Cobertura (Coverage):** **100% absoluto** en líneas, métodos y ramas (Branch Coverage) en todos los paquetes del subdominio financiero y común (`contract`, `payment`, `subscription`, `common`).

---

## Conclusiones y Siguientes Pasos
El núcleo de negocio (Dominio) está completo, inmutable y testeado al máximo nivel. La plataforma está lista para avanzar a la **Semana 3 (Capa de Aplicación)**, donde se construirán los Casos de Uso (`UseCases`) que orquestarán estas piezas perfectas para darles vida frente a los eventos del mundo exterior.