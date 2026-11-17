# AGENTS.md — Contexto Persistente para IA

## Stack Tecnológico

- **Java 21** + **Spring Boot 3.4.x**
- **Thymeleaf** + **Bootstrap 5** (vistas renderizadas en servidor)
- **Spring Data JPA** + **MySQL 8.x**
- **Spring Security** (5 roles: CLIENTE, CAJERO, PASTELERO, REPARTIDOR, ADMIN)
- **Maven** (wrapper incluido: `./mvnw`)

## Estructura de Paquetes Permitidos

```
com.caketrack/
├── model/          # Entidades JPA (@Entity), DTOs, enums
├── repository/     # Interfaces JpaRepository
├── controller/     # @Controller (retornan nombres de plantillas Thymeleaf)
└── config/         # Configuración Spring Security, JPA, Web MVC
```

## Comandos Esenciales

```bash
# Compilar proyecto
./mvnw clean compile

# Ejecutar pruebas
./mvnw test

# Ejecutar aplicación (requiere MySQL corriendo)
./mvnw spring-boot:run
```

## Prohibiciones Críticas

### 🚫 CERO CAPA SERVICE

**Estrictamente prohibido** crear:
- Paquete `com.caketrack.service`
- Carpetas llamadas `service`
- Clases con sufijo `*Service`

Las reglas de negocio se implementan en:
- Métodos de entidades (`@Entity`)
- Controladores (`@Controller`)
- Repositorios (`JpaRepository`)

### 🚫 CERO APIs REST

**Prohibido**:
- `@RestController`
- `@ResponseBody` en controladores (excepto para AJAX interno)
- Endpoints que retornen JSON/XML como respuesta principal
- Frontends separados (React, Angular, Vue)

**Obligatorio**:
- `@Controller` retorna nombre de plantilla Thymeleaf
- Vistas renderizadas en servidor con Bootstrap 5

## Diseño visual (obligatorio)

- Toda vista HTML/Thymeleaf nueva o modificada debe consultar `/specs/design.md` antes de implementarse.
- Usar los estilos de `/css/caketrack.css` y sus tokens en `:root`. No introducir colores, fuentes, radios ni sombras fuera de esos tokens.
- Las acciones son contornos rojos de 1px, sin relleno. No usar sombras.
- Al cambiar estilos, no alterar atributos `th:*`, `sec:authorize` ni los `name` de los formularios.

## Documentos de Gobierno

- **Constitución**: `/specs/constitution.md` (7 principios no negociables)
- **Especificaciones**: `/specs/spec.md` (historias de usuario EARS)
- **Decisiones ADR**: `/docs/adr/` (justificación de dependencias y arquitectura)
- **Bitácora IA**: `/ia/bitacora.md` (registro de interacciones y contexto)

## Flujo de Trabajo SDD (Spec-Driven Development)

1. Escribir requisitos en `/specs/spec.md` (sintaxis EARS)
2. Definir criterios de aceptación Given/When/Then
3. Crear pruebas automatizadas (JUnit 5 + MockMvc)
4. Implementar código de producción
5. Registrar decisiones en `/docs/adr/` si aplica
6. Actualizar `/ia/bitacora.md` con cambios significativos

## Convenciones de Git

- Commits referencian tareas: `T### - Descripción corta`
- Branches: `feature/T###-descripcion`, `fix/T###-descripcion`
- Merge a `main` solo después de revisión humana

---

**Última actualización**: 2026-11-16
