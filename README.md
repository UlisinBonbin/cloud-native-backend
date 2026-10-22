# 🧸 Peluchitos Bonbin — Backend

Backend de **Peluchitos Bonbin**, desarrollado con **Spring Boot** bajo una arquitectura de microservicios con patrón **BFF (Backend for Frontend)**.

Gestiona usuarios, productos, stock, carritos y pedidos, utilizando **AWS Cognito** para autenticación, **AWS API Gateway** como punto de entrada y **Amazon RDS MySQL** como base de datos.

---

## 📑 Tabla de contenidos

- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Componentes](#-componentes)
- [Autenticación y autorización](#-autenticación-y-autorización)
- [Roles](#-roles)
- [Principales endpoints](#-principales-endpoints)
- [Flujo de compra](#-flujo-de-compra)
- [Estados de pedido](#-estados-de-pedido)
- [Base de datos](#-base-de-datos)
- [Configuración](#-configuración)
- [Ejecución local](#-ejecución-local)
- [Compilación](#-compilación)
- [Comunicación interna](#-comunicación-interna)
- [Seguridad](#-seguridad)
- [Infraestructura AWS](#-infraestructura-aws)
- [Git y archivos sensibles](#-git-y-archivos-sensibles)
- [Contexto académico](#-contexto-académico)

---

## 🏗️ Arquitectura

El BFF **no accede directamente** a la base de datos. Cada microservicio gestiona su propia lógica de negocio y accede a RDS mediante su propio repositorio.

```text
                         INTERNET
                            │
                            ▼
                    FRONTEND REACT
                            │
                            │ Access Token JWT
                            ▼
                  AWS API GATEWAY
                  JWT Authorizer
                            │
                            ▼
                           BFF
                    Spring Boot :8080
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
       producto-service pedido-service usuario-service
            :8081            :8083          :8082
             │              │              │
             └──────────────┼──────────────┘
                            ▼
                      AWS RDS MySQL
```

---

## 🚀 Tecnologías

| Categoría        | Tecnologías                                   |
|-------------------|------------------------------------------------|
| Lenguaje / Runtime | Java 21                                        |
| Framework          | Spring Boot, Spring Security, Spring Data JPA  |
| ORM                | Hibernate                                      |
| Build              | Maven                                          |
| Base de datos      | MySQL (Amazon RDS)                             |
| Identidad          | AWS Cognito                                    |
| Puerta de entrada  | AWS API Gateway                                |
| Seguridad          | JWT / OAuth 2.0 Resource Server                |

---

## 🧩 Componentes

### 🔀 BFF — `:8080`

Intermediario entre React y los microservicios.

**Responsabilidades:**
- Recibir solicitudes del frontend
- Validar nuevamente el Access Token JWT
- Propagar el JWT hacia los microservicios
- Comunicarse con `producto-service`, `pedido-service` y `usuario-service`
- Exponer una API adaptada al frontend
- No acceder directamente a RDS

### 📦 producto-service — `:8081`

**Responsabilidades:**
- Gestión del catálogo (crear, consultar, actualizar, eliminar productos)
- Gestión y descuento de stock

> Los endpoints de administración de productos requieren el rol `ADMINISTRADOR`.

### 🛒 pedido-service — `:8083`

**Responsabilidades:**
- Gestión de carritos (agregar, modificar, eliminar productos, comprar)
- Descontar stock mediante `producto-service`
- Obtener pedidos del usuario
- Gestión de pedidos y cambio de estado por parte del operador

> Los endpoints de gestión general de pedidos requieren el rol `OPERADOR`.

### 👤 usuario-service — `:8082`

**Responsabilidades:**
- Obtener y actualizar información del usuario autenticado
- Vincular usuarios mediante el `cognitoSub`

### 🛠️ admin-service — `:8084`

**Responsabilidades:**
- Microservicio administrador de mensajería (RabbitMQ).
- Exponer API REST para administración de colas (`/queues`), exchanges (`/exchanges`) y bindings (`/bindings`).
- Encapsular la lógica en `RabbitAdminService`.
- Validar parámetros de entrada mediante DTOs y Bean Validation.

---

## 🔐 Autenticación y autorización

La autenticación se realiza mediante **AWS Cognito**, que emite los tokens utilizados por la aplicación.

```text
Usuario
   ↓
AWS Cognito
   ↓
Access Token JWT
   ↓
API Gateway
   ↓
Validación JWT
   ↓
BFF
   ↓
Nueva validación JWT
   ↓
Microservicio
   ↓
Validación JWT + autorización
```

### API Gateway

El **JWT Authorizer** usa la configuración de Cognito para validar los tokens antes de permitir el acceso a rutas protegidas. También gestiona enrutamiento y CORS.

### BFF (OAuth2 Resource Server)

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${COGNITO_ISSUER}
spring.security.oauth2.resourceserver.jwt.audiences=${API_AUDIENCE}
```

Esto permite validar nuevamente firma, issuer, audience y vigencia del token.

### Microservicios

Cada microservicio también es un OAuth2 Resource Server y aplica reglas de autorización por rol:


.hasRole("ADMINISTRADOR")
.hasRole("OPERADOR")


Los grupos de Cognito se transforman en *authorities* mediante un `CognitoJwtAuthenticationConverter`:

```text
cognito:groups = ["ADMINISTRADOR"]
            ↓
ROLE_ADMINISTRADOR
```

---

## 🔑 Roles

| Funcionalidad              | Usuario | Operador | Administrador |
|------------------------------|:-------:|:--------:|:--------------:|
| Consultar catálogo            | ✅      | ✅       | ✅             |
| Carrito                       | ✅      | ✅       | ✅             |
| Comprar                       | ✅      | ✅       | ✅             |
| Consultar sus pedidos         | ✅      | ✅       | ✅             |
| Ver todos los pedidos         | ❌      | ✅       | ❌             |
| Cambiar estado de pedidos     | ❌      | ✅       | ❌             |
| Crear productos               | ❌      | ❌       | ✅             |
| Editar productos              | ❌      | ❌       | ✅             |
| Eliminar productos            | ❌      | ❌       | ✅             |

> La seguridad de los endpoints protegidos se aplica en el backend y no depende solamente de la interfaz de usuario.

---

## 🔌 Principales endpoints

### Productos

```text
GET    /api/v1/productos
GET    /api/v1/productos/{id}

POST   /api/v1/productos
PUT    /api/v1/productos/{id}
DELETE /api/v1/productos/{id}

POST   /api/v1/productos/{id}/stock
```

La consulta de productos es pública; las operaciones administrativas requieren `ADMINISTRADOR`.

### Carrito

```text
GET    /api/v1/pedidos/carrito
POST   /api/v1/pedidos/carrito/productos
PUT    /api/v1/pedidos/carrito/productos/{productoId}
DELETE /api/v1/pedidos/carrito/productos/{productoId}
POST   /api/v1/pedidos/carrito/comprar
```

### Pedidos

```text
GET    /api/v1/pedidos/mis-pedidos
GET    /api/v1/pedidos
PUT    /api/v1/pedidos/{id}/estado
```

> Los últimos dos endpoints están destinados a la gestión del operador.

### Usuarios

```text
GET    /api/v1/usuarios/me
PUT    /api/v1/usuarios/me
```

---

## 🛍️ Flujo de compra

```text
React
  ↓
POST /api/v1/pedidos/carrito/comprar
  ↓
API Gateway
  ↓
BFF
  ↓
pedido-service
  ↓
Consulta carrito en RDS
  ↓
Consulta stock en producto-service
  ↓
Descuento de stock
  ↓
Actualiza pedido a PAGADO
  ↓
Guarda cambios en RDS
  ↓
Respuesta al frontend
```

> El stock se descuenta únicamente cuando el usuario realiza la compra, no al agregar un producto al carrito.

---

## 📊 Estados de pedido

```text
CARRITO
PAGADO
PENDIENTE
EN_PREPARACION
ENVIADO
ENTREGADO
CANCELADO
```

El operador puede actualizar el estado desde el panel correspondiente.

---

## 🗄️ Base de datos

El backend utiliza **Amazon RDS MySQL**. Entidades principales:

```text
usuarios
productos
pedidos
pedido_items
```

Cada microservicio implementa:

- Entidades JPA
- Repositories de Spring Data JPA
- Services para reglas de negocio
- Controllers para exponer las APIs

---

## ⚙️ Configuración

Cada servicio tiene sus propias propiedades. Ejemplo — configuración del BFF:

```properties
spring.application.name=bff-service
server.port=8080

producto-service.url=http://10.0.133.181:8081
usuario-service.url=http://10.0.133.181:8082
pedido-service.url=http://10.0.133.181:8083

spring.security.oauth2.resourceserver.jwt.issuer-uri=${COGNITO_ISSUER}
spring.security.oauth2.resourceserver.jwt.audiences=${API_AUDIENCE}
```

> 🚫 Las credenciales y secretos no deben almacenarse directamente en el repositorio.

---

## 💻 Ejecución local

```bash
# Con Maven Wrapper
./mvnw spring-boot:run

# O con el JAR generado
java -jar target/*.jar
```

| Servicio          | Puerto |
|--------------------|:------:|
| BFF                | 8080   |
| producto-service   | 8081   |
| usuario-service    | 8082   |
| pedido-service     | 8083   |
| admin-service      | 8084   |

---

## 🐰 Mensajería Asíncrona (RabbitMQ)

Se implementa una arquitectura orientada a eventos con **RabbitMQ**, cumpliendo con la pauta de evaluación:

### 1. Configuración Centralizada (`application.yml`)
Todas las colas, exchanges y routing keys se definen de forma estandarizada en `application.yml` y clases `@Configuration` (`RabbitMQConfig`):
- **Exchange principal**: `peluchitos.exchange` (TopicExchange)
- **Dead Letter Exchange (DLX)**: `peluchitos.dlx` (DirectExchange)
- **Caso 1: Pedido Pagado**:
  - Cola: `pedido.pagado.queue` (con DLQ configurada)
  - DLQ: `pedido.pagado.dlq`
  - Routing Key: `pedido.pagado.key`
  - Routing Key DLQ: `pedido.pagado.dlq.key`
- **Caso 2: Pedido Cancelado**:
  - Cola: `pedido.cancelado.queue` (con DLQ configurada)
  - DLQ: `pedido.cancelado.dlq`
  - Routing Key: `pedido.cancelado.key`
  - Routing Key DLQ: `pedido.cancelado.dlq.key`

### 2. Consumidores por Dominio y Confirmación Explícita (ACK / DLQ)
En `producto-service`:
- Paquete: `com.peluchin.producto_service.consumer`
- Consumidor: `InventarioConsumer` implementado con `@RabbitListener` en el contenedor `AcknowledgeMode.MANUAL`.
- Confirmación de éxito: `channel.basicAck(deliveryTag, false)`.
- Manejo explícito de errores: Ante datos inválidos o fallos de stock, se rechaza con `channel.basicNack(deliveryTag, false, false)` sin requeue para dirigir el mensaje a la cola de letras muertas (**DLQ**).
- Consumidor de auditoría: `InventarioDlqConsumer` monitorea los mensajes fallidos en la DLQ.

### 3. API REST del Microservicio Administrador (`admin-service` — :8084)
La lógica está encapsulada en `RabbitAdminService` y los endpoints cuentan con validación de parámetros (Bean Validation):
- `POST /queues` o `/api/v1/admin/queues`: Crear cola con configuración (durable, exclusivo, DLQ).
- `GET /queues/{name}` o `/api/v1/admin/queues/{name}`: Consultar información y conteo de mensajes de una cola.
- `DELETE /queues/{name}` o `/api/v1/admin/queues/{name}`: Eliminar cola.
- `POST /queues/{name}/purge`: Purgar mensajes de una cola.
- `POST /exchanges`: Crear exchange (`direct`, `topic`, `fanout`, `headers`).
- `DELETE /exchanges/{name}`: Eliminar exchange.
- `POST /bindings`: Crear binding entre cola y exchange.
- `DELETE /bindings`: Eliminar binding.

---

## 🐳 Docker y Docker Compose

El proyecto cuenta con contenedorización completa para desarrollo local y despliegues en AWS EC2:

### Estructura de archivos Docker
- **`docker-compose.yml`**: A nivel de raíz, orquesta RabbitMQ y todos los microservicios.
- **`Dockerfile`**: Ubicado dentro de cada microservicio (`bff-service`, `producto-service`, `pedido-service`, `usuario-service`, `admin-service`), con compilación multi-stage basada en `eclipse-temurin:21` y ejecución con usuario no-root por seguridad.

### Opciones de ejecución con Docker

#### Opción A: Levantar únicamente RabbitMQ (Broker)
Si deseas ejecutar los microservicios directamente con `./mvnw spring-boot:run` o desde el IDE:
`ash
docker compose up -d rabbitmq
`
- Consola Web de RabbitMQ: http://localhost:15672 (Usuario: guest / Clave: guest)
- Broker AMQP: localhost:5672

#### Opción B: Levantar todo el ecosistema (RabbitMQ + 5 Microservicios)
1. Configura tus variables en un archivo .env basándote en .env.example
2. Compila y levanta todos los contenedores:
`ash
docker compose up -d --build
`

#### Detener los contenedores
`ash
docker compose down
`


---

## 🏗️ Compilación

```bash
./mvnw clean package -DskipTests
```

El JAR se genera en `target/`.

---

## 🔗 Comunicación interna

El BFF se comunica con los microservicios mediante REST, propagando el token del usuario:

```http
Authorization: Bearer <access_token>
```

Ejemplo:

```text
BFF → http://10.0.133.181:8081  (producto-service)
BFF → http://10.0.133.181:8083  (pedido-service)
```

Esto permite que cada microservicio realice sus propias validaciones y autorizaciones.

---

## 🛡️ Seguridad

El sistema aplica varias capas de validación:

```text
1. AWS Cognito       → Autenticación y emisión de JWT
2. API Gateway       → Validación inicial del JWT
3. BFF               → Validación adicional del JWT
4. Microservicios    → Validación del JWT + autorización por rol
```

De esta forma, un usuario no puede obtener permisos administrativos simplemente modificando la interfaz frontend.

---

## ☁️ Infraestructura AWS

- Amazon Cognito
- Amazon API Gateway
- Amazon EC2
- Amazon RDS
- VPC
- Security Groups

Los microservicios se ejecutan en infraestructura EC2, y el BFF actúa como intermediario entre API Gateway y los servicios internos.

---

## 🚫 Git y archivos sensibles

No se deben subir al repositorio:

```text
*.pem
.env con secretos
contraseñas
credenciales de AWS
archivos temporales
target/
logs/
```

Se recomienda un `.gitignore` apropiado para Java/Spring Boot y manejar las credenciales mediante variables de entorno o mecanismos seguros de configuración.

---

## 🎓 Contexto académico

Este backend forma parte de la solución **Peluchitos Bonbin**, desarrollada con AWS como plataforma cloud y Spring Boot para los componentes backend. La arquitectura demuestra:

- Autenticación mediante un proveedor de identidad
- Uso de Access Token JWT
- Validación de JWT en múltiples capas
- Autorización basada en roles
- API Gateway y patrón BFF
- Arquitectura de microservicios
- Persistencia en una base de datos cloud
- Reglas de negocio para catálogo, carrito, stock y pedidos
