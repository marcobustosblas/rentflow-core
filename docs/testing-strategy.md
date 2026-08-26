# Estrategia de Testing y Garantía de Integridad Financiera - Semana 3

## 1. Visión General y Arquitectura QA

En **RentFlowAI**, la capa de aplicación y orquestación de negocio (`usecase`) constituye el puente crítico entre el mundo exterior (pasarelas de pago Webpay/Stripe, webhooks, peticiones HTTP) y las reglas sagradas del dominio financiero. 

Para garantizar un software de grado bancario y multi-tenant seguro, hemos diseñado una **Estrategia de Testing de Alta Fidelidad** utilizando **JUnit 5**, **Mockito** y el motor de cobertura **JaCoCo**.

### Objetivos Clave de la Estrategia:
- **Cero Inconsistencias Financieras:** Ningún pago insuficiente, duplicado o en divisa errónea puede ser procesado o liquidado.
- **Protección Multitenant (Anti-IDOR):** Ningún usuario puede interactuar con propiedades, contratos o cuotas que no le pertenezcan.
- **Resiliencia ante Eventos Asíncronos:** Idempotencia garantizada frente a múltiples disparos de Webhooks o clics repetidos del inquilino.

---

## 2. Desglose de Pruebas Unitarias por Caso de Uso

### 2.1. `CreateContractUseCase` — Creación y Activación de Contratos

Este caso de uso formaliza el acuerdo legal entre el Propietario (`Landlord`) y el Inquilino (`Tenant`), vinculando una `Property` y transitando su estado a `RENTED`.

| Prueba Unitaria | Categoría QA / Por qué de la prueba | Impacto de Negocio |
| :--- | :--- | :--- |
| `shouldThrowExceptionWhenLandlordMismatch` | **Protección contra IDOR (Insecure Direct Object Reference):** Verifica que el `landlordId` suministrado coincida exactamente con el dueño legal de la propiedad. | Impide que un propietario malicioso o un error en el frontend cree contratos sobre inmuebles ajenos. |
| `shouldThrowExceptionWhenUserIsNotTenant` | **Validación Estricta de Roles:** Garantiza que el usuario asignado como inquilino tenga el rol de `Role.TENANT`. | Evita inconsistencias de perfil y accesos indebidos a tableros de pago. |
| `shouldThrowExceptionWhenPropertyNotAvailable` | **Prevención de Doble Arriendo:** Valida que la propiedad esté en estado `AVAILABLE`. | Previene litigios legales al impedir arrendar dos veces la misma propiedad. |
| `shouldThrowExceptionWhenPropertyNotFound` / `shouldThrowExceptionWhenTenantNotFound` | **Defensa de Integridad Referencial:** Comprueba la existencia previa de las entidades en la BD antes de cualquier mutación. | Evita registros huérfanos o corrupción en la base de datos. |
| `shouldCreateContractSuccessfully` | **Prueba de Orquestación Exitosa:** Verifica que al crear el contrato se marque la propiedad como `RENTED`, se persistan ambas entidades y se retorne el agregado. | Asegura la correcta ejecución del flujo principal de entrada a la plataforma. |

---

### 2.2. `InitiatePaymentCheckoutUseCase` — Generación de Checkout de Pago

Orquesta el inicio del proceso de cobro mensual, calculando los reajustes y multas por mora antes de redirigir al inquilino a la pasarela de pago (Webpay/Stripe).

| Prueba Unitaria | Categoría QA / Por qué de la prueba | Impacto de Negocio |
| :--- | :--- | :--- |
| `shouldThrowExceptionWhenTenantIsNotOwner` | **Protección IDOR en Pagos:** Valida que únicamente el `Tenant` titular del contrato pueda iniciar un proceso de cobro sobre dicho contrato. | Garantiza la privacidad financiera e impide que terceros generen intenciones de pago sobre contratos ajenos. |
| `shouldReuseExistingPendingPaymentWhenAvailable` | **Escudo de Idempotencia y Prevención de Duplicidad:** Filtra mediante `p.isPending() && p.getDueDate().equals(dueDate)`. Si ya existe una intención pendiente para ese mes, la reutiliza sin guardar un nuevo registro. | **Vital:** Previene cobros duplicados y múltiples órdenes de pago abiertas cuando el inquilino hace "doble clic" o recarga la página. |
| `shouldCreateNewPaymentRecordWhenNoPendingExists` | **Flujo Inicial de Generación de Cobro:** Crea la intención de pago `PaymentRecord` con clave determinista `PAY-{contractId}-{YYYY}-{MM}` y obtiene la URL segura del Gateway. | Garantiza la disponibilidad del botón de pago para el inquilino en la fecha adecuada. |

---

### 2.3. `ProcessPaymentUseCase` — Procesamiento de Pagos y Confirmación por Webhook

Procesa la notificación asíncrona (Webhook) o retorno de la pasarela de pago para liquidar la cuota mensual.

| Prueba Unitaria | Categoría QA / Por qué de la prueba | Impacto de Negocio |
| :--- | :--- | :--- |
| `shouldThrowExceptionWhenPaymentNotFound` | **Protección contra Tokens Fantasmas:** Rechaza webhooks con claves de idempotencia inexistentes o alteradas. | Bloquea ataques de suplantación de pagos mediante simulación de webhooks. |
| `shouldReturnExistingPaymentWhenAlreadyPaid` | **Idempotencia Absoluta de Transacción:** Si el pago ya se encuentra en estado `PAID`, se retorna inmediatamente sin volver a modificar la BD ni reenviar correos. | Protege el sistema ante reintentos automáticos de Pasarelas de Pago (retries de Stripe/Webpay) evitando duplicidad de ingresos en la contabilidad. |
| `shouldThrowExceptionWhenPaymentIsInsufficient` | **Protección contra Hackeo de Montos (Underpayment):** Compara el monto pagado vs el monto esperado (Arriendo + Multas congeladas). | Evita fraudes en los que un usuario modifica el formulario frontend para pagar \$1 en lugar de \$500.000. |
| `shouldThrowExceptionWhenCurrencyMismatch` | **Escudo de Divisas (Multi-currency Shield):** Valida que la moneda del pago enviado coincida con la moneda de la cuota esperada (ej: CLP vs USD). | Previene pérdidas por discrepancias cambiarias o intentos de pago en monedas de menor valor. |
| `shouldProcessPaymentSuccessfullyWithReference` | **Liquidación Financiera y Notificación:** Registra el pago como `PAID`, vincula el código de transacción bancario (`transactionReference`), guarda el comprobante y dispara la notificación por correo al arrendatario. | Cierra el ciclo mensual de cobro de forma transparente y legalmente respaldada. |

---

## 3. Matriz de Cobertura y Métricas de Calidad

- **Cobertura de Casos de Uso:** 100% de los métodos de orquestación de la Capa de Aplicación.
- **Tolerancia a Fallos:** Verificación mediante `Mockito.verify(repo, never()).save(...)` en todos los caminos alternativos y excepciones.
- **Aislamiento Arquitectónico:** 0% de acoplamiento a bases de datos o controladores web. Pruebas ejecutadas en **milisegundos**.
