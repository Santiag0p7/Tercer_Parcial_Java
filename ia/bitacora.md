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
