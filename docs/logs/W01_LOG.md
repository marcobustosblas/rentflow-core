# Bitácora de Desarrollo - Semana 1: Núcleo de Dominio y Arquitectura Hexagonal

## Resumen Ejecutivo
Durante la Semana 1, se estableció y blindó el núcleo de la aplicación (`rentflow-core`) bajo los principios de **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal (Ports and Adapters)**.
El objetivo principal fue modelar las entidades de negocio críticas (`User` y `Property`), aplicar reglas de validación estrictas adaptadas al mercado chileno (como el formato y manejo del RUT) y garantizar una calidad de código inquebrantable mediante pruebas unitarias exhaustivas con un **100% de cobertura de líneas y ramas**.

---

## ¿Por qué Arquitectura Hexagonal (Ports & Adapters)?

Elegimos Arquitectura Hexagonal para **RentFlowAI** por tres razones fundamentales de ingeniería de software:

1. **Independencia de Frameworks y Bases de Datos:**
   El Dominio no sabe que existe Spring Boot, Hibernate, PostgreSQL, Docker o la Web. Si el día de mañana decidimos cambiar PostgreSQL por otra base de datos o migrar la API, **el Core no cambia en una sola línea de código**.
2. **Testabilidad de Alta Velocidad (TDD Puro):**
   Podemos ejecutar decenas de pruebas de reglas de negocio en milisegundos sin necesidad de levantar contenedores Docker, bases de datos o servidores Tomcat.
3. **Aislamiento de Reglas de Negocio:**
   Previene el "código espagueti" donde la lógica financiera o de arriendos se mezcla con consultas SQL o anotaciones de controladores HTTP.

---

## Componentes de la Arquitectura Hexagonal

```text
               [ MUNDO EXTERIOR ]
         (HTTP REST / React / CLI)
                     │
                     ▼ (Llama a través de Ports IN)
      ┌─────────────────────────────┐
      │     CAPA DE APLICACIÓN      │
      │        (Use Cases)          │
      │  ┌───────────────────────┐  │
      │  │     CAPA DOMINIO      │  │
      │  │ (User, Property, DDD) │  │
      │  └───────────────────────┘  │
      └─────────────────────────────┘
                     │
                     ▼ (Accede a través de Ports OUT)
         [ ADAPTADORES EXTERNOS ]
         (PostgreSQL, Spring JPA, AWS)
```

### 1. El Dominio (domain/) — "El Rey"
   Es el centro inmutable del hexágono. Contiene los Agregados, Entidades, Enums y Excepciones de Negocio. Aquí residen las reglas sagradas del sistema (por ejemplo: validación de RUT, transiciones de estado de propiedades y roles).

### 2. La Aplicación (application/usecase/) — "Los Orquestadores"
   Contiene los Casos de Uso (ej. CreatePropertyUseCase, RegisterUserUseCase). Coordinan el flujo de datos: reciben la petición, invocan la lógica del dominio y le piden a los puertos que guarden la información.

### 3. Los Puertos (application/port/) — "Los Contratos"
   Son interfaces puras de Java que definen cómo se comunica el Core con el exterior:

- Ports IN (Entrada): Interfaces que definen lo que el mundo exterior puede pedirle al sistema (Casos de Uso).

- Ports OUT (Salida): Interfaces que definen lo que el Core necesita del mundo exterior (ej. UserRepository, PropertyRepository).

### 4. La Infraestructura (infrastructure/) — "Los Sirvientes"
   Contiene los Adaptadores (Controllers de Spring, Entidades JPA, Repositorios de PostgreSQL, Clientes Feign, Pasarelas de Pago). Implementan los Ports OUT y adaptan los datos externos hacia el formato que entiende el Dominio.

## Decisiones Técnicas y de Diseño
### 1. Agregado Raíz User
- Identidad y Atributos: Identificador inmutable (UUID), credenciales encriptadas y un conjunto normalizado de roles (Set<Role>).

- Manejo del RUT Chileno: Opcional al nacer (registro rápido) pero protegido con métodos estrictos de validación (updateRut) con Expresiones Regulares (Regex) para garantizar el formato 12345678-9.

- Constructores Delegados: Encadenamiento de constructores para inicializar auditorías (createdAt, updatedAt) y estados por defecto (ACTIVE).

### 2. Agregado Raíz Property
- Inmutabilidad: Atributo landlordId marcado como final para evitar la transferencia no autorizada de propiedades entre dueños.

- Comportamientos: Métodos encapsulados para cambio de precios base, actualización de direcciones, asignación de cuenta bancaria (payoutAccountId) y estados (AVAILABLE, RENTED, MAINTENANCE).

### 3. Puertos de Salida y Excepciones
- Interfaces puras UserRepository y PropertyRepository definidas en ports.out.

- Excepciones semánticas (UserNotFoundException, PropertyNotFoundException).

## Métricas de Calidad y Cobertura (JaCoCo)
- Pruebas Unitarias: 32 tests pasando en verde con JUnit 5, @Nested y @ParameterizedTest.

- Cobertura de Código: 100% de líneas, métodos y ramas (Branch Coverage) alcanzado en los paquetes de dominio de User y Property.

## Conclusiones y Siguientes Pasos
El núcleo de la Semana 1 queda oficialmente cerrado y blindado. 
El proyecto está listo para iniciar la Semana 2: Lógica Financiera (RentalContract, PaymentRecord, BankAccount y Subscription).