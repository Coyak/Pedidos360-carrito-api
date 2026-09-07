# Microservicio Carrito API - Pedidos360

Microservicio desarrollado en **Java 17** con **Spring Boot** para la gestión del carrito de compras y procesamiento transaccional de pedidos en el sistema **Pedidos360**, integrado con **Spring Security OAuth2 Resource Server** para la validación de tokens JWT de **Microsoft Entra ID (Azure AD)**.

---

## 🚀 Requisitos Previos

- **Java**: JDK 17 o superior
- **Maven**: 3.8+ o Maven Wrapper
- **Puerto por defecto**: `8081`

---

## ⚙️ Configuración del Entorno (`application.properties`)

El servicio está configurado para ejecutarse en el puerto **8081** y autenticarse contra Microsoft Entra ID:

```properties
server.port=8081
spring.application.name=carrito-api

# Configuración de OAuth2 / Resource Server con Microsoft Entra ID
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://sts.windows.net/e5372bf0-c5e3-4286-887c-79069f209c1f/
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://login.microsoftonline.com/e5372bf0-c5e3-4286-887c-79069f209c1f/discovery/v2.0/keys
```

---

## 🛠️ Cómo Compilar y Ejecutar

1. **Compilar el proyecto**:
   ```bash
   mvn clean package -DskipTests
   ```

2. **Ejecutar localmente**:
   ```bash
   mvn spring-boot:run
   ```
   O bien ejecutando el paquete JAR generado:
   ```bash
   java -jar target/carrito-api-1.0.0.jar
   ```

---

## 📌 Endpoints REST Disponibles

### 1. Estado del Servicio (Público)
- **Método:** `GET`
- **Ruta:** `/api/carrito/status`
- **Acceso:** Libre (sin autenticación)
- **Respuesta de Ejemplo (`200 OK`):**
```json
{
  "status": "OK",
  "message": "Microservicio de Carrito y Pedidos en ejecución",
  "port": "8081",
  "version": "1.0.0"
}
```

### 2. Registro de Pedido (Protegido)
- **Método:** `POST`
- **Ruta:** `/api/carrito/pedidos`
- **Acceso:** Protegido (requiere cabecera `Authorization: Bearer <JWT_TOKEN>`)
- **Cuerpo de la Petición (`application/json`):**
```json
{
  "clienteEmail": "usuario@duocuc.cl",
  "direccionEnvio": "Av. España 8, Santiago",
  "items": [
    {
      "productoId": "PROD-001",
      "nombreProducto": "Laptop Gamer",
      "cantidad": 1,
      "precioUnitario": 990000.00
    }
  ]
}
```
- **Respuesta Exitosa (`201 CREATED`):**
```json
{
  "pedidoId": "PED-8F3A2B1C",
  "status": "PROCESADO",
  "clienteEmail": "usuario@duocuc.cl",
  "direccionEnvio": "Av. España 8, Santiago",
  "totalPagar": 990000.0,
  "fechaCreacion": "2026-09-07T18:45:00",
  "items": [ ... ]
}
```

---

## 🏷️ Versionamiento Semántico (SemVer)

- **`v1.0.0`**: Inicialización del microservicio Carrito API, configuración de Spring Security OAuth2 Resource Server con Entra ID y endpoints `/api/carrito/status` y `/api/carrito/pedidos`.
