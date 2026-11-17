# Bitácora de IA — CakeTrack

Registro de interacciones, decisiones y contextos relevantes compartidos con asistentes de IA.

## Formato de entrada

```
### YYYY-MM-DD — Tema
- **Contexto**: …
- **Decisión**: …
- **Resultado**: …
```

---

### 2026-11-17 — T001 y T002: configuración base y modelo de datos
- **Contexto**: Implementación de las tareas T001 y T002 de `specs/tasks.md` (Sprint 1).
- **Decisión**: Entidades JPA con métodos de dominio (sin capa service). `PasswordEncoder` declarado temporalmente en `config/PasswordEncoderConfig.java`, a migrar a `SecurityConfig` en T004. `TipoProducto` añadido como enum adicional (requerido por `productos.tipo`). Seed de datos en `DataInitializer` protegido por verificación de tabla vacía (idempotente).
- **Resultado**: `./mvnw test-compile` exitoso con Java 21. Arranque con MySQL pendiente de verificación (servidor MySQL no disponible en el equipo durante la implementación).

### 2026-11-17 — T003 y T004: pruebas y seguridad de autenticación
- **Contexto**: Pruebas de autenticación, recuperación y control por rol escritas antes de la implementación (fallaron en rojo: 10 de 13). Luego implementación de `SecurityConfig`, `AuthController` y plantillas Thymeleaf.
- **Decisión**: `UserDetailsService` como bean en `SecurityConfig` consultando `UsuarioRepository` (sin clase `service`). `PasswordEncoder` movido de `PasswordEncoderConfig` a `SecurityConfig`; la clase temporal se eliminó. Usuario inactivo redirige a `/login?inactivo` mediante `failureHandler`. Reglas de `/admin/ingredientes/**` y `/admin/productos/**` permiten PASTELERO (según plan, matriz de permisos), y `/admin/usuarios/**` solo ADMIN. Pool de Hikari limitado a 2 conexiones por el límite de 5 conexiones simultáneas de Clever Cloud.
- **Pendiente**: el enlace de recuperación se escribe en el log del servidor porque el envío de correo está fuera de alcance (no-objetivos del Sprint 1). Debe reemplazarse cuando se incorpore el envío de correo.
- **Resultado**: `./mvnw test` con 14 pruebas en verde (13 de `AuthSecurityTest` y 1 de `CakeTrackApplicationTests`).

### 2026-11-17 — T005 a T010: usuarios, inventario, recetas y catálogo
- **Contexto**: Pruebas escritas primero (19 fallas en rojo antes de implementar) y luego implementación de `AdminUsuarioController`, `InventarioController` y `CatalogoController`, con sus vistas bajo `fragments/layout`.
- **Decisión**: Validaciones en controladores con parámetros simples (sin clases DTO ni service). Protección para que un ADMIN no desactive su propia cuenta ni se quite su rol. Al quitar el último ingrediente de un producto disponible, el producto pasa a no disponible (BR-05). Alerta de stock mínimo como insignia roja "Reponer" (BR-04 define un único estado de alerta). Filtro por tipo en el catálogo (AC-007.2).
- **Pendiente**: edición de nombre y precio de productos no está implementada (no aparece en los criterios de aceptación del Sprint 1). Verificación visual en navegador pendiente.
- **Resultado**: `./mvnw test` con 33 pruebas en verde.

### 2026-11-17 — Preparación para despliegue en Render (Docker)
- **Contexto**: Dockerfile multi-etapa, `.dockerignore` y ADR-002. Se retiraron de `application.properties` el host y el usuario reales de Clever Cloud.
- **Decisión**: Credenciales por variables `DB_URL`, `DB_USER` y `DB_PASS` (reemplazan a `DB_PASSWORD`). Puerto por `PORT`. Pool de Hikari en 5 conexiones.
- **Resultado**: `./mvnw test` con 33 pruebas en verde. La imagen Docker no se construyó localmente porque Docker no está instalado en el equipo.
