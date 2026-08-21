# 📖 Diccionario Técnico de RentFlowAI

Este documento consolida los conceptos avanzados de Arquitectura, Seguridad, Inteligencia Artificial y Cloud aplicados en el desarrollo de RentFlow Core (RFC).

---

## Arquitectura y Diseño de Software 🏛️

* **DDD (Domain-Driven Design):** Enfoque de desarrollo donde el código se estructura en torno a las reglas de negocio del mundo real (el "Dominio") antes de pensar en bases de datos o frameworks. En RFC, es tu carpeta `core/domain`.
* **Arquitectura Hexagonal (Ports & Adapters):** Patrón que aísla el núcleo de tu aplicación del mundo exterior. El núcleo no sabe si se conecta a PostgreSQL, a una Web o a una API. Se comunica a través de **Puertos** (Interfaces) y **Adaptadores** (Implementaciones como Spring Data JPA o Controllers).
* **Agregado Raíz (Aggregate Root):** La entidad principal que actúa como "jefe" de un grupo de objetos relacionados. En RFC, `User`, `Property` y `RentalContract` son Agregados. Cualquier cambio a sus datos internos debe pasar por ellos.
* **Value Object (Objeto de Valor):** Un objeto inmutable que se define por sus atributos y no por su identidad. Si dos `Rut` tienen el valor "12345678-9", son exactamente el mismo. Ejemplos en RFC: `Money` y `Rut`.
* **Factory Method (Método de Fábrica):** Un patrón de diseño donde se oculta el constructor (`private`) y se expone un método estático (`registerNew`, `create`) con un nombre que explica claramente la intención de negocio al crear el objeto.
* **Caso de Uso (Use Case):** Orquestador de la capa de Aplicación. Recibe una orden del exterior, busca los datos mediante repositorios, ejecuta reglas del dominio y guarda los resultados. No contiene reglas matemáticas, solo coordina.
* **DTO (Data Transfer Object):** Un objeto simple y tonto que solo sirve para transportar datos desde tu Backend hacia el Frontend (o viceversa) sin exponer las verdaderas Entidades de tu base de datos.

---

## Seguridad y Resiliencia 🔐

* **IDOR (Insecure Direct Object Reference):** Vulnerabilidad crítica donde un atacante cambia un ID en la URL o payload (ej. `propertyId=999`) y logra acceder o modificar datos de otro usuario porque el sistema no validó si él era el dueño real de ese recurso.
* **Idempotencia:** Propiedad matemática y de software que garantiza que realizar una acción una vez tiene el mismo efecto que realizarla múltiples veces. En RFC, usamos el `idempotencyKey` para evitar que un inquilino pague dos veces si hace "doble clic" o si hay un error de red.
* **JWT (JSON Web Token):** Un estándar seguro para autenticar usuarios de forma "Stateless" (sin guardar sesiones en la memoria del servidor). Es un token encriptado que viaja en las cabeceras HTTP demostrando quién es el usuario y qué roles tiene.

---

## Testing y Calidad de Código 🧪

* **TDD (Test-Driven Development):** Práctica de escribir la prueba unitaria *antes* de escribir el código de producción.
* **Mock / Mocking (Mockito):** Crear un objeto falso o "doble de riesgo" que simula el comportamiento de una base de datos o API externa. Sirve para probar la lógica de tus Casos de Uso sin necesidad de tener PostgreSQL encendido.
* **Code Coverage (Cobertura de Código / JaCoCo):** Métrica que indica qué porcentaje de tu código fue ejecutado por tus pruebas.
* **Branch Coverage (Cobertura de Ramas):** Métrica estricta que verifica si tus pruebas pasaron por todos los caminos lógicos posibles de un `if`, `switch` o un operador lógico (`&&`, `||`).

---

## Inteligencia Artificial (AI Developer) 🤖

* **Agentic AI (IA Agéntica):** Sistemas de inteligencia artificial que no solo responden preguntas (como un chatbot), sino que tienen autonomía para usar herramientas (ej. buscar en bases de datos o ejecutar código) y tomar decisiones para cumplir un objetivo.
* **LLM (Large Language Model):** Modelo de lenguaje masivo entrenado con trillones de palabras (como GPT-4 o Claude). En RFC, es el "cerebro" al que le enviamos el PDF del contrato para que lea y extraiga los datos financieros.
* **System Prompt:** Las instrucciones base e invisibles que se le dan a un modelo de IA para definir su personalidad, reglas y formato de respuesta (ej. "Eres un auditor legal. Responde solo en JSON").
* **RAG (Retrieval-Augmented Generation):** Técnica donde, antes de preguntarle algo a la IA, el sistema busca documentos internos en una base de datos vectorial y se los pasa a la IA para que su respuesta sea precisa y no alucine. (Lo aplicaremos en la Etapa 2 de REVA).

---

##  Cloud y DevOps ☁️

* **SaaS (Software as a Service):** Modelo de negocio donde el software se licencia por suscripción y se aloja en la nube de forma centralizada (ej. RentFlowAI).
* **CI/CD (Continuous Integration / Continuous Deployment):** Práctica de automatizar las pruebas (CI) y el despliegue del código a los servidores (CD) cada vez que haces un `git push`. (Lo haremos con GitHub Actions).
* **Docker / Contenerización:** Tecnología que empaqueta tu aplicación Java y todas sus dependencias en una "caja" (contenedor) que funcionará exactamente igual en tu laptop que en los servidores de AWS.