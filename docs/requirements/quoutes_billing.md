#  Especificación de Cuotas, Planes y Facturación SaaS (RentFlowAI B2B Chile)

**Reglas de Negocio, Precios en Pesos Chilenos (CLP) y Límites de Plataforma**

---

## 1. Visión General del Modelo de Precios B2B

RentFlowAI opera bajo un modelo de suscripciones B2B adaptado al mercado inmobiliario chileno. El esquema de tarifas está optimizado para ofrecer un costo altamente competitivo por propiedad administrada (~$9.200 - $9.500 CLP/mes/propiedad), con facturación mensual o anual (2 meses gratis en modalidad anual).

---

## 2. Matriz Tarifaria Oficial

| Plan | Canon Mensual | Canon Anual (10 meses cobrados) | Límite Propiedades (`propertyLimit`) | Almacenamiento PDF (`maxStorageMb`) | Funcionalidades Incluidas |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **STARTER** | **$27.900 CLP / mes** | $279.000 CLP / año | Hasta 3 propiedades | 100 MB | Recaudación básica, reajuste IPC automatizado, recordatorios automáticos por WhatsApp. |
| **PRO** | **$64.900 CLP / mes** | $649.000 CLP / año | Hasta 7 propiedades | 1.000 MB (1 GB) | Todo Starter + Lectura OCR de comprobantes con IA, Webhooks automáticos. |
| **ENTERPRISE** | **$119.900 CLP / mes** | $1.199.000 CLP / año | Hasta 13 propiedades | 10.000 MB (10 GB) | Todo Pro + Auditoría de contratos PDF con Spring AI, Multi-cuenta bancaria de liquidación, soporte prioritario. |

---

## 3. Reglas e Invariantes de Negocio

### RN-SUB-01: Control Estricto de Límite de Propiedades (`canAddProperty`)
- **Regla:** El dominio evalúa la condición `currentPropertyCount <= maxProperties`. Si el propietario alcanza o supera el límite de su plan activo, el intento de crear una nueva propiedad es bloqueado inmediatamente arrojando una excepción de negocio (`IllegalStateException`) que la capa REST mapea a HTTP 403 Forbidden.

### RN-SUB-02: Equivocalidad de Facturación Anual (Descuento de 2 Meses Gratis)
- **Regla:** Seleccionar `BillingCycle.YEARLY` cobra el equivalente a 10 cánones mensuales en una sola transacción, extendiendo la validez de la suscripción por 365 días (`subscriptionPeriodEnd = startDate.plusYears(1)`).

### RN-SUB-03: Asignación de Almacenamiento Documental en MB
- **Regla:** El espacio disponible para la carga de contratos PDF se actualiza automáticamente al cambiar de plan:
  - `STARTER`: 100 MB.
  - `PRO`: 1.000 MB.
  - `ENTERPRISE`: 10.000 MB.